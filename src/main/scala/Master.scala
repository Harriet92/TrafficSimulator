import akka.actor.{ActorRef, Actor}

class Master extends Actor {
  override def receive: Receive = {

    case Car.FieldEnterMessage(x, y) => println("Dupa")
    case Car.FieldLeaveMessage(x, y) => println("Dupa")
    case Car.FieldQueryMessage(x, y, dx, dy) => println("Dupa")
    case _ => println("Dupa")

  }
}

object Master {

  trait FieldType

  object Road extends FieldType
  object Obstacle extends FieldType

  case class FieldInfo(t: FieldType, car: ActorRef, crossing: ActorRef)

  case class FieldInfoMessage(info: FieldInfo, x: Int, y: Int)

}