import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Crossing(opt: Crossing.Options) extends Actor with ActorLogging {

  var horizontalWaitingCars: List[ActorRef] = List() 
  var verticalWaitingCars: List[ActorRef] = List()
  val stateProvider = new Crossing.StateProvider(opt)

  override def preStart(): Unit = {

      setScheduler()
  }

  override def receive: Receive = {

    case Car.LightQuery(direction) =>
      log.info("Crossing: Received LightQuery")
      val dir = if(direction == TopDirection || direction == BottomDirection) Crossing.Vertical else Crossing.Horizontal
      val canGo = stateProvider.isGreen(dir)
      if(canGo) sender ! Crossing.GreenColorMessage()
      else dir match{
        case Crossing.Vertical => verticalWaitingCars :+ sender
        case Crossing.Horizontal => horizontalWaitingCars :+ sender
      }

    case Crossing.ChangeTrafficLights =>
      log.info("Crossing: Received ChangeTrafficLights")
      stateProvider.nextState()
      setScheduler()
      if(stateProvider.isGreen(Crossing.Vertical))
        verticalWaitingCars.foreach(car => car ! Crossing.GreenColorMessage())
      else if(stateProvider.isGreen(Crossing.Horizontal))
        horizontalWaitingCars.foreach(car => car ! Crossing.GreenColorMessage())
      
    case _ => log.warning("Crossing: Unexpected message!")
  }

  def setScheduler() {

    context.system.scheduler.scheduleOnce(
      delay = stateProvider.currentDuration,
      receiver = self,
      message = Crossing.ChangeTrafficLights)
  }
}

object Crossing {

  def props(opt: Options): Props = Props(new Crossing(opt))

  sealed abstract class LightState
  object RedLight extends LightState
  object GreenLight extends LightState
  object OrangeLight extends LightState
  object OrangeRedLight extends LightState

  case class Options(hGreenDuration: FiniteDuration, vGreenDuration: FiniteDuration, orangeDuration: FiniteDuration){

    def this(duration: FiniteDuration, orangeDuration: FiniteDuration){
      this(duration, duration, orangeDuration)
    }

    def this(){
      this(10 seconds, 2 seconds )
    }
  }
  case class GreenColorMessage()
  case class ChangeTrafficLights()
  case class Direction()
  object Horizontal extends Direction
  object Vertical extends Direction

  case class StateElem(hstate: LightState, vstate: LightState, duration: FiniteDuration)

  class StateProvider(opt: Options){
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

    def isGreen(direction: Direction): Boolean = direction match{
        case Horizontal => states(counter).hstate equals GreenLight
        case Vertical => states(counter).vstate equals GreenLight
      }

    def nextState() = {
      counter = (counter + 1) % states.length
    }

  }

}