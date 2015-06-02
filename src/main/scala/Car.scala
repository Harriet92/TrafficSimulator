import akka.actor.{ActorRef, ActorLogging, Actor}
import scala.concurrent.ExecutionContext.Implicits.global

class Car(var currentLoc: Location, var targetLoc: Location, master: ActorRef) extends Actor with ActorLogging {

  var waitingCars: List[ActorRef] = List()
  var currentFieldIsCrossing: Boolean = false
  var currentDirection: RoadDirection = NoDirection
  var checkedAllDirections: Boolean = false
  var excludedDirections: RoadDirection = NoDirection
  val velocity = Consts.carVelocity

  override def receive: Receive = {
    case Master.Start() => init()

    case Car.WaitingForFieldMessage(loc) =>
      //log.info("Received WaitingForFieldMessage")
      handleOtherCarWaitMessage(loc)

    case Master.FieldInfoMessage(direction, car, crossing) =>
      //log.info("Received FieldInfoMessage")
      handleFieldInfoMessage(direction, car, crossing)

    case Crossing.GreenColorMessage =>
      //log.info("Received GreenColorMessage")
      master ! Car.FieldQueryMessage(currentLoc, calculateDirections())

    case Car.MoveFinished => continueMovement()

    case Car.FieldFree(loc, queue) =>
      //log.info("Received FieldFreeMessage!")
      handleFieldFree(loc, queue)

    case Master.NextTargetMessage(newLoc) =>
      //log.info(s"Received new target location: $newLoc")
      changeTarget(newLoc)

    case _ => log.info("Not recognized message!")
  }

  def init(): Unit = {
    setScheduler()
  }

  private def handleFieldInfoMessage(direction: RoadDirection, car: ActorRef, crossing: ActorRef): Unit = {
    if(direction == NoDirection) {
      checkedAllDirections = true
      excludedDirections = NoDirection
      master ! Car.FieldQueryMessage(currentLoc, calculateDirections())
    } else {
      // we just arrived in front of a crossroad
      currentDirection = direction
      if (crossing != null && !currentFieldIsCrossing) {
        currentFieldIsCrossing = true
        crossing ! Car.LightQuery(direction)
      }
      // we are on a crossroad and behave like on a road
      else {
        if (crossing == null && currentFieldIsCrossing) currentFieldIsCrossing = false
        val newLoc = direction.applyMovement(currentLoc)
        if (car != null) {
          if(checkedAllDirections) {
            car ! Car.WaitingForFieldMessage(newLoc)
            checkedAllDirections = false
          } else {
            excludedDirections = excludedDirections + direction
            master ! Car.FieldQueryMessage(currentLoc, calculateDirections())
          }
        }
        else startMovement(newLoc)
      }
    }
  }


  def handleFieldFree(loc:Location, refs: List[ActorRef]): Unit = {
    startMovement(loc)
    waitingCars = refs
  }

  private def handleOtherCarWaitMessage(loc: Location) = {
    if (currentLoc == loc && !waitingCars.contains(sender()))
      waitingCars = sender :: waitingCars
    else if (loc != currentLoc)
      sender ! Car.FieldFree(loc, Nil)
  }

  private def changeTarget(location: Location): Unit = {
    targetLoc = location
    master ! Car.FieldQueryMessage(currentLoc, calculateDirections())
  }

  private def startMovement(newLoc: Location) = {
    log.info(s"Moving to $newLoc")
    master ! Car.FieldEnterMessage(newLoc)
    sendFieldFreeMessage()
    currentLoc = newLoc
    setScheduler()
  }

  private def sendFieldFreeMessage() : Unit = {
    if (waitingCars.nonEmpty) {
      waitingCars.head ! Car.FieldFree (currentLoc, waitingCars.tail)
      waitingCars = List ()
    }
  }

  private def continueMovement() = {
    log.info(s"Arrived to $currentLoc")
    if(currentLoc == targetLoc) {
      log.info(s"Reached target location: $targetLoc")
      master ! Car.DestinationReachedMessage()
      sendFieldFreeMessage()
    }
    else
      master ! Car.FieldQueryMessage(currentLoc, calculateDirections())
  }

  private def calculateDirections(): List[RoadDirection] = {
    val sigX = if(targetLoc.x - currentLoc.x >= 0) 1 else -1
    val sigY = if(targetLoc.y - currentLoc.y >= 0) 1 else -1
    val deltaXbiggerThanDeltay = math.abs(currentLoc.x - targetLoc.x) > math.abs(currentLoc.y -targetLoc.y)

    val priorityList = Car.directionPriorities((sigX, sigY, deltaXbiggerThanDeltay))
    priorityList.filter((rd) => rd != currentDirection.reverse() && !excludedDirections.contains(rd))
  }

  private def setScheduler() {
    log.debug("Setting scheduler")
    context.system.scheduler.scheduleOnce(
      delay = velocity,
      receiver = self,
      message = Car.MoveFinished)
  }
}

object Car {

  case class FieldQueryMessage(loc: Location, directions: List[RoadDirection])
  case class FieldEnterMessage(loc: Location)
  case class WaitingForFieldMessage(loc: Location)
  case class FieldFree(loc:Location, queue: List[ActorRef])
  case class LightQuery(direction: RoadDirection)
  case class MoveFinished()
  case class DestinationReachedMessage()

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
