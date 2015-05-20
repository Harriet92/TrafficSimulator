import akka.actor.{ActorRef, Props, ActorLogging, Actor}

class Master() extends Actor with ActorLogging {

  val (map, crossings) = MapLoader.fileLoader("map.txt").loadMap(context)

  override def receive: Receive = {

    case Car.FieldEnterMessage(x, y) => println("FieldEnterMessage")
    case Car.FieldQueryMessage(x, y, dir) => handleQuery(x, y, dir)
    case _ => println("Dupa")

  }

  def handleQuery(x: Int, y: Int, directions: List[RoadDirection]): Unit = {
    log.info(s"Received FieldQueryMessage $x, $y, $directions")
    val bestDir = directions.find(map.getOrElse((x, y), NoDirection).contains).getOrElse(NoDirection)
    val crossing = crossings.getOrElse(x -> y, null)
    if (bestDir != NoDirection)
      sender ! Master.FieldInfoMessage(bestDir, null, crossing)
  }

}

object Master {

  def props = Props[Master]

  case class FieldInfoMessage(direction: RoadDirection, car: ActorRef, crossing: ActorRef)

}