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
  var nextFieldIsCrossing: Boolean = false

  def enterField(newX: Int, newY: Int) = {
    master ! Car.FieldLeaveMessage(currentX, currentY)
    master ! Car.FieldEnterMessage(newX, newY)
    currentX = newX
    currentY = newY
    waitingCars.head ! Car.FieldFree(waitingCars.tail)
    waitingCars = List()
  }

  def shutdown() = {
    // TODO
  }

  def findPath() = {
    if(currentX == targetX && currentY == targetY)
      shutdown()
    // TODO
    master ! Car.FieldQueryMessage(currentX, currentY, dx, dy)
  }

  override def receive: Receive = {

    case Car.WaitingForField(x, y) => {
      if(x == currentX && y == currentY && !waitingCars.contains(sender)) waitingCars :+ sender
      else println("Incorrect position!")
    }

    case Master.FieldInfoMessage(Master.FieldInfo(t, car, crossing), x, y) => t match {
      case Master.Road => {
        // przed skrzyzowaniem ...
        if(crossing != null) {
          // kontunuacja skrzyzowania
          if(currentFieldIsCrossing) {
            if(car != null) car ! Car.WaitingForField(x, y)
            else enterField(x, y)
          }
          // przed skrzyzowaniem, ale samochod jeszcze o tym nie wie
          else if(!nextFieldIsCrossing) {
            crossing ! Car.LightQuery(x, y)
            nextFieldIsCrossing = true
          }
          // nie ma miejsca
          else if(car != null) car ! Car.WaitingForField(x, y)
          // wjazd na skrzyzowanie
          else {
            enterField(x, y)
            currentFieldIsCrossing = true
            nextFieldIsCrossing = false
          }
        }
        // brak skrzyzowania
        else if(car == null) {
          enterField(x, y)
          currentFieldIsCrossing = false
        }
      }
      case Master.Obstacle => {}
    }
    case Crossing.LightColorMessage(state) => state match {
      case Crossing.GreenLight => master ! Car.FieldQueryMessage(currentX, currentY, dx, dy)
      case Crossing.OrangeLight => {}
      case Crossing.RedLight => {}
    }
    case _ => println("Incorrect traffic color!")

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
