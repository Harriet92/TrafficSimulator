import java.awt.Color

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class Crossing(opt: Crossing.Options, drawer: ActorRef) extends Actor with ActorLogging {

  var horizontalWaitingCars: List[ActorRef] = List()
  var verticalWaitingCars: List[ActorRef] = List()
  val stateProvider = new Crossing.StateProvider(opt)

  override def receive: Receive = {
    case Master.Start() => init()

    case Car.LightQuery(direction) =>
      //log.info("Crossing: Received LightQuery")
      handleLightQuery(direction)

    case Crossing.ChangeTrafficLights =>
      //log.info("Crossing: Received ChangeTrafficLights")
      handleLightChange()
      drawer ! Crossing.TrafficLightsChanged(stateProvider.currentState())
      setScheduler()

    case _ => log.warning("Crossing: Unexpected message!")
  }

  def init(): Unit = {
    setScheduler()
    drawer ! Crossing.TrafficLightsChanged(stateProvider.currentState())
  }
  private def handleLightChange(): Unit = {
    stateProvider.nextState()
    if (stateProvider.isGreen(Crossing.Vertical)) {
      verticalWaitingCars.foreach(car => car ! Crossing.GreenColorMessage)
      verticalWaitingCars = List()
    }
    else if (stateProvider.isGreen(Crossing.Horizontal)) {
      horizontalWaitingCars.foreach(car => car ! Crossing.GreenColorMessage)
      horizontalWaitingCars = List()
    }
  }

  private def handleLightQuery(direction: RoadDirection): Unit = {
    val dir = Crossing.roadDirectionToDirection(direction)
    if (stateProvider.isGreen(dir))
      sender ! Crossing.GreenColorMessage
    else dir match {
      case Crossing.Vertical => verticalWaitingCars = sender :: verticalWaitingCars
      case Crossing.Horizontal => horizontalWaitingCars = sender :: horizontalWaitingCars
    }
  }

  def setScheduler() {
    context.system.scheduler.scheduleOnce(
      delay = stateProvider.currentDuration,
      receiver = self,
      message = Crossing.ChangeTrafficLights)
  }
}

object Crossing {

  def props(opt: Options, drawer: ActorRef): Props = Props(new Crossing(opt, drawer))

  trait LightState

  object RedLight extends LightState

  object GreenLight extends LightState

  object OrangeLight extends LightState

  object OrangeRedLight extends LightState

  case class CurrentState(hstate: LightState, vstate: LightState)

  trait Direction

  object Horizontal extends Direction

  object Vertical extends Direction

  object GreenColorMessage

  object ChangeTrafficLights

  case class TrafficLightsChanged(currState: CurrentState)

  def roadDirectionToDirection(dir: RoadDirection): Direction =
    if (dir.contains(TopDirection) || dir.contains(BottomDirection))
      Vertical
    else
      Horizontal

  case class Options(hGreenDuration: FiniteDuration, vGreenDuration: FiniteDuration, orangeDuration: FiniteDuration) {

    def this(duration: FiniteDuration = 10 seconds, orangeDuration: FiniteDuration = 2 seconds) {
      this(duration, duration, orangeDuration)
    }

  }

  val lightStateToColor = Map[LightState, Color](
    RedLight -> Color.RED,
    GreenLight -> Color.GREEN,
    OrangeLight -> Color.ORANGE,
    OrangeRedLight -> Color.ORANGE
  )

  class StateProvider(opt: Options) {

    case class StateElem(hstate: LightState, vstate: LightState, duration: FiniteDuration)

    val states = Array(
      new StateElem(GreenLight, RedLight, opt.hGreenDuration),
      new StateElem(OrangeLight, RedLight, opt.orangeDuration),
      new StateElem(RedLight, OrangeRedLight, opt.orangeDuration),
      new StateElem(RedLight, GreenLight, opt.vGreenDuration),
      new StateElem(RedLight, OrangeLight, opt.orangeDuration),
      new StateElem(OrangeRedLight, RedLight, opt.orangeDuration))

    var counter = 0

    def currentDuration: FiniteDuration = {
      states(counter).duration
    }

    def isGreen(direction: Direction): Boolean = direction match {
      case Horizontal => states(counter).hstate equals GreenLight
      case Vertical => states(counter).vstate equals GreenLight
    }

    def currentState(): CurrentState = CurrentState(states(counter).hstate, states(counter).vstate)

    def nextState() = {
      counter = (counter + 1) % states.length
    }

  }

}