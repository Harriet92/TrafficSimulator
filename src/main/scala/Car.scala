import akka.actor.{Cancellable, ActorRef, ActorLogging, Actor}
import scala.concurrent.ExecutionContext.Implicits.global

class Car(var currentLoc: Location, var targetLoc: Location, master: ActorRef) extends Actor with ActorLogging {

  var waitingCars: List[ActorRef] = List()
  var currentFieldIsCrossing: Boolean = false
  var currentDirection: RoadDirection = NoDirection
  val velocity = Consts.carVelocity
  var cancellableWaitingScheduler: Option[Cancellable] = None
  var frontCar: Option[ActorRef] = None

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

    case Car.NoLongerWaitingForFieldMessage(loc) =>
      handleNoLongerWaiting(loc)

    case _ => log.info("Not recognized message!")
  }

  def init(): Unit = {
    setScheduler()
  }

  private def handleFieldInfoMessage(direction: RoadDirection, car: ActorRef, crossing: ActorRef): Unit = {
    finishWaiting()
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
        frontCar = Some(car)
        car ! Car.WaitingForFieldMessage(newLoc)
        setWaitingScheduler()
      }
      else startMovement(newLoc)
    }
  }

  def handleFieldFree(loc:Location, refs: List[ActorRef]): Unit = {
    finishWaiting()
    startMovement(loc)
    waitingCars = refs
  }

  private def handleOtherCarWaitMessage(loc: Location) = {
    if (currentLoc == loc && !waitingCars.contains(sender()))
      waitingCars = sender :: waitingCars
    else if (loc != currentLoc)
      sender ! Car.FieldFree(loc, Nil)
  }

  private def handleNoLongerWaiting(loc: Location): Unit = {
    if(loc == currentLoc)
      waitingCars = waitingCars.filter(car => car != sender)
  }

  private def changeTarget(location: Location): Unit = {
    targetLoc = location
    master ! Car.FieldQueryMessage(currentLoc, calculateDirections())
  }

  private def startMovement(newLoc: Location) = {
    log.info(s"Moving to $newLoc")
    master ! Car.FieldEnterMessage(newLoc)
    if (waitingCars.nonEmpty) {
      waitingCars.head ! Car.FieldFree(currentLoc, waitingCars.tail)
      waitingCars = List()
    }
    currentLoc = newLoc
    setScheduler()
  }

  private def continueMovement() = {
    log.info(s"Arrived to $currentLoc")
    if(currentLoc == targetLoc) {
      log.info(s"Reached target location: $targetLoc")
      master ! Car.DestinationReachedMessage()
    }
    else
      master ! Car.FieldQueryMessage(currentLoc, calculateDirections())
  }

  private def calculateDirections(): List[RoadDirection] = {
    val sigX = if(targetLoc.x - currentLoc.x >= 0) 1 else -1
    val sigY = if(targetLoc.y - currentLoc.y >= 0) 1 else -1
    val deltaXbiggerThanDeltay = math.abs(currentLoc.x - targetLoc.x) > math.abs(currentLoc.y -targetLoc.y)

    val priorityList = Car.directionPriorities((sigX, sigY, deltaXbiggerThanDeltay))
    priorityList.filter((rd) => rd != currentDirection.reverse())
  }

  private def setScheduler() {
    log.debug("Setting scheduler")
    context.system.scheduler.scheduleOnce(
      delay = velocity,
      receiver = self,
      message = Car.MoveFinished)
  }

  private def setWaitingScheduler(): Unit = {
    log.debug("Setting waiting scheduler")
    cancellableWaitingScheduler = Some(context.system.scheduler.scheduleOnce(
      delay = Consts.waitingSchedulerSpan,
      receiver = master,
      message = Car.FieldQueryMessage(currentLoc, calculateDirections().filter(dir => dir != currentDirection))))
  }

  private def finishWaiting(): Unit = {
    if(cancellableWaitingScheduler.isDefined) {
      cancellableWaitingScheduler.get.cancel()
      cancellableWaitingScheduler = None
    }
    if(frontCar.isDefined) {
      frontCar.get ! Car.NoLongerWaitingForFieldMessage(currentDirection.applyMovement(currentLoc))
      frontCar = None
    }
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
  case class NoLongerWaitingForFieldMessage(loc: Location)

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
