import akka.actor.{ActorRef, ActorLogging, Actor}

class Car(var currentX: Int,
          var currentY: Int,
          targetX: Int,
          targetY: Int,
          master: ActorRef) extends Actor with ActorLogging {

  var dx: Int = 0
  var dy: Int = 0
  var waitingCars: List[ActorRef] = List()
  var currentFieldIsCrossing: Boolean = false

  def enterField(newX: Int, newY: Int) = {
    master ! Car.FieldLeaveMessage(currentX, currentY)
    master ! Car.FieldEnterMessage(newX, newY)
    currentX = newX
    currentY = newY
    waitingCars.head ! Car.FieldFree(waitingCars.tail)
    waitingCars = List()
  }

  def findPath() = {
    if(currentX == targetX && currentY == targetY)
      context.system.stop(self)
    // TODO
    master ! Car.FieldQueryMessage(currentX, currentY, dx, dy)
  }

  override def receive: Receive = {

    case Car.WaitingForField(x, y) =>
      if(x == currentX && y == currentY && !waitingCars.contains(sender()))
        waitingCars :+ sender
      else if(x != currentX || y != currentY)
        sender ! Car.FieldFree(null)

    case Master.FieldInfoMessage(Master.FieldInfo(t, car, crossing), x, y) => t match {
      case Master.Road =>
        // info o skrzyzowaniu do ktorego wlasnie dojezdzamy
        if(crossing != null && !currentFieldIsCrossing) {

          currentFieldIsCrossing = true
          crossing ! Car.LightQuery(x, y)

        //jestesmy na skrzyzowaniu - normalny ruch
        else {
          if(crossing == null && currentFieldIsCrossing) currentFieldIsCrossing = false

          if(car != null) car ! Car.WaitingForField(x, y)
          else enterField(x, y)
        }
      }
      case Master.Obstacle =>
    }
    case Crossing.GreenColorMessage() =>  master ! Car.FieldQueryMessage(currentX, currentY, dx, dy)
  }
}

object Car {

  case class FieldQueryMessage(x: Int, y: Int, dx: Int, dy: Int)
  case class FieldEnterMessage(x: Int, y: Int)
  case class FieldLeaveMessage(x: Int, y: Int)
  case class WaitingForField(x: Int, y: Int)
  case class FieldFree(queue: List[ActorRef])
  case class LightQuery(x: Int, y: Int)
}
