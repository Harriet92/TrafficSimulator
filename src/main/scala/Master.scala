import akka.actor.{Props, ActorLogging, Actor}

class Master() extends Actor with ActorLogging {

  val map = MapLoader.fileLoader("map.txt").loadMap
  val crossings = Crossing.createCrossings(map)

  override def receive: Receive = {

    case Car.FieldEnterMessage(x, y) => println("Dupa")
    case Car.FieldLeaveMessage(x, y) => println("Dupa")
    case Car.FieldQueryMessage(x, y, dx, dy) => println("Dupa")
    case _ => println("Dupa")

  }

}

object Master {

  val props = Props[Master]

  trait FieldType

  case object Road extends FieldType
  case object Obstacle extends FieldType

  case class FieldInfo(t: FieldType, car: Car, crossing: Crossing)

  case class FieldInfoMessage(info: FieldInfo, x: Int, y: Int)

}