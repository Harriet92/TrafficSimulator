import akka.actor.{ActorRef, ActorLogging, Actor}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class Car(var currentX: Int,
          var currentY: Int,
          targetX: Int,
          targetY: Int,
          master: ActorRef) extends Actor with ActorLogging {


  var waitingCars: List[ActorRef] = List()
  var currentFieldIsCrossing: Boolean = false
  var currentDirection: RoadDirection = LeftDirection
  val velocity = 1 seconds

  override def receive: Receive = {

    case Car.WaitingForField(x, y) =>
      if(x == currentX && y == currentY && !waitingCars.contains(sender()))
        waitingCars :+ sender
      else if(x != currentX || y != currentY)
        sender ! Car.FieldFree(null)

    case Master.FieldInfoMessage(direction, car, crossing) =>
        // we just arrived in front of a crossroad
        if(crossing != null && !currentFieldIsCrossing) {

          currentFieldIsCrossing = true
          crossing ! Car.LightQuery(direction)
        }
        // we are on a crossroad and behave like on a road
        else {

          if (crossing == null && currentFieldIsCrossing) currentFieldIsCrossing = false
          val (newX, newY) = direction.applyMovement(currentX, currentY)
          if (car != null) car ! Car.WaitingForField(newX, newY)
          else startMovement(newX, newY)
        }

    case Crossing.GreenColorMessage() =>  master ! Car.FieldQueryMessage(currentX, currentY, calculateDirections())
    case Car.MoveFinished => continueMovement()
  }

  def startMovement(newX: Int, newY: Int) = {
    master ! Car.FieldEnterMessage(newX, newY)
    waitingCars.head ! Car.FieldFree(waitingCars.tail)
    waitingCars = List()
    currentX = newX
    currentY = newY
    setScheduler()
  }

  def continueMovement() = {
    master ! Car.FieldQueryMessage(currentX, currentY, calculateDirections())
  }

  def calculateDirections(): List[RoadDirection] = {

    val sigX = if(targetX - currentX >= 0) 1 else 0
    val sigY = if(targetY - currentY >= 0) 1 else 0
    val deltaXbiggerThanDeltay = math.abs(currentX - targetX) > math.abs(currentY - targetY)

    val priorityList = Car.directionPriorities((sigX, sigY, deltaXbiggerThanDeltay))
    priorityList.filter((rd) => rd != currentDirection)
  }

  def setScheduler() {

    context.system.scheduler.scheduleOnce(
      delay = velocity,
      receiver = self,
      message = Car.MoveFinished)
  }
}

object Car {

  case class FieldQueryMessage(x: Int, y: Int, directions: List[RoadDirection])
  case class FieldEnterMessage(x: Int, y: Int)
  case class WaitingForField(x: Int, y: Int)
  case class FieldFree(queue: List[ActorRef])
  case class LightQuery(direction: RoadDirection)
  case class MoveFinished()

  val directionPriorities = Map[(Int, Int, Boolean), List[RoadDirection]](
    (1, 1, true) -> List(RightDirection, TopDirection, BottomDirection, LeftDirection),
    (1, 1, false) -> List(TopDirection, RightDirection, LeftDirection, BottomDirection),
    (1, -1, true) -> List(RightDirection, BottomDirection, TopDirection, LeftDirection),
    (1, -1, false) -> List(BottomDirection, RightDirection, LeftDirection, TopDirection),
    (-1, 1, true) -> List(LeftDirection, TopDirection, BottomDirection, RightDirection),
    (-1, 1, false) -> List(TopDirection, LeftDirection, RightDirection, BottomDirection),
    (-1, -1, true) -> List(LeftDirection, BottomDirection, TopDirection, RightDirection),
    (-1, -1, false) -> List(BottomDirection, LeftDirection, RightDirection, TopDirection)
  )
}
