import akka.actor.{ActorLogging, Actor}

class Car extends Actor with ActorLogging {
  override def receive: Receive = {

    case Car.WaitingForField(x, y) => println("Dupa")
    case Master.FieldInfoMessage(Master.FieldInfo(t, car, crossing), x, y) => println("Dupa")
    case Crossing.LightColorMessage(state) => state match {
      case Crossing.RedLight => println("Dupa")
      case Crossing.GreenLight => println("Dupa")
      case Crossing.OrangeLight => println("Dupa")
    }
    case _ => println("Dupa")

  }
}

object Car {

  case class FieldQueryMessage(x: Int, y: Int, dx: Int, dy: Int)
  case class FieldEnterMessage(x: Int, y: Int)
  case class FieldLeaveMessage(x: Int, y: Int)
  case class WaitingForField(x: Int, y: Int)
  case class FieldFree(queue: List[Car])
  case class LightQuery(x: Int, y: Int)

}
