import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import scala.concurrent.duration
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

class Master(drawer: ActorRef) extends Actor with ActorLogging {

  private val random = new Random()
  val (map, crossings) = MapLoader.fileLoader("map.txt").loadMap(context, drawer)
  drawer ! Master.DrawMap(map, crossings)
  val possibleLocations = map.keys.filter(crossings.get(_) == None).toIndexedSeq
  val cars = createCars(10)
  val searchTargetDelay = new FiniteDuration(20, duration.SECONDS)

  override def receive: Receive = {
    case Car.FieldEnterMessage(loc) =>
      log.info(s"Received FieldEnterMessage $loc")
      handleFieldEnterMessage(loc)
      drawer ! Master.RefreshCars(cars)

    case Car.FieldQueryMessage(loc, dir) =>
      log.info(s"Received FieldQueryMessage $loc, $dir")
      handleQuery(loc, dir)

    case Car.DestinationReachedMessage() =>
      log.info(s"Received DestinationReachedMessage!")
      handleDestinationReached()

    case _ => println("Dupa")
  }

  def handleQuery(loc: Location, directions: List[RoadDirection]): Unit = {
    val bestDir = directions.find(map.getOrElse(loc, NoDirection).contains).getOrElse(NoDirection)
    val newLoc = bestDir.applyMovement(loc)
    val car = cars.find(_._2 == newLoc).map(_._1)
    val crossing = crossings.getOrElse(newLoc, null)
    if (bestDir != NoDirection)
      sender ! Master.FieldInfoMessage(bestDir, car.orNull, crossing)
  }

  def handleFieldEnterMessage(location: Location): Unit = {
    cars(sender()) = location
  }

  def handleDestinationReached(): Unit = {
    context.system.scheduler.scheduleOnce(
      delay = searchTargetDelay,
      receiver = sender(),
      message = new Master.NextTargetMessage(findRandomPlaceOnRoad()))
  }

  def findRandomPlaceOnRoad(): Location = {
    possibleLocations(random.nextInt(possibleLocations.length))
  }

  def createCars(number: Int): mutable.Map[ActorRef, Location] = {
    def createCar(leftToCreate: Int, currentList: List[(ActorRef, Location)]): List[(ActorRef, Location)] = {
      if(leftToCreate > 0) {
        val startingPos = findRandomPlaceOnRoad()
        val targetPos = findRandomPlaceOnRoad()
        log.info(s"Creating car with starting location $startingPos")
        val newCar = context.actorOf(Props(new Car(startingPos, targetPos, self)))
        createCar(leftToCreate - 1, newCar -> startingPos :: currentList)
      } else {
        currentList
      }
    }
    //val testCar1 = context.actorOf(Props(new Car(new Location(0, 1), new Location(6, 1), self))) -> new Location(0, 1)
    //val testCar2 = context.actorOf(Props(new Car(new Location(2, 1), new Location(4, 1), self))) -> new Location(2, 1)

    //mutable.Map(List(testCar1, testCar2): _*)
    mutable.Map(createCar(number, List.empty): _*)
  }

}

object Master {

  def props = Props[Master]

  case class FieldInfoMessage(direction: RoadDirection, car: ActorRef, crossing: ActorRef)
  case class NextTargetMessage(loc: Location)
  case class DrawMap(var map: Map[Location, RoadDirection], var crossings: Map[Location, ActorRef])
  case class RefreshCars(var cars : mutable.Map[ActorRef, Location])

}