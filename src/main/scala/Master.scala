import akka.actor.{ActorRef, Props, ActorLogging, Actor}

class Master() extends Actor with ActorLogging {

  val (map, crossings) = MapLoader.fileLoader("map.txt").loadMap(context)
  map.foreach(kv => println("Field: %s, %s -- %s".format(kv._1._1, kv._1._2, kv._2)))

  override def receive: Receive = {

    case Car.FieldEnterMessage(x, y) => println("FieldEnterMessage")
    case Car.FieldQueryMessage(x, y, dir) => handleQuery(x, y, dir)
    case _ => println("Dupa")

  }

  def handleQuery(x: Int, y: Int, directions: List[RoadDirection]): Unit = {
    log.info(s"Received FieldQueryMessage $x, $y, $directions")
    sender ! Master.FieldInfoMessage(directions.find(map.getOrElse((x, y), new RoadDirection(false, false, false, false)).contains).get, null, null)
  }

}

object Master {

  def props = Props[Master]

  case class FieldInfoMessage(direction: RoadDirection, car: ActorRef, crossing: ActorRef)

}