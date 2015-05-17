import akka.actor.{ActorRef, Props, ActorLogging, Actor}

class Master() extends Actor with ActorLogging {

  val map = MapLoader.fileLoader("map.txt").loadMap

  override def receive: Receive = {

    case Car.FieldEnterMessage(x, y) => println("Dupa")
    case Car.FieldQueryMessage(x, y, dir) => println("Dupa")
    case _ => println("Dupa")

  }

}

object Master {

  def props = Props[Master]

  case class FieldInfoMessage(direction: RoadDirection, car: ActorRef, crossing: ActorRef)

}