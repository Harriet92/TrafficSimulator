import akka.actor.Actor

class Crossing extends Actor {
  override def receive: Receive = {

    case Car.LightQuery(x, y) => println("Dupa")
    case _ => println("Dupa")

  }
}

object Crossing {

  sealed abstract class LightState(val colour: String, val id: String) {
    override val toString: String = s"${colour}LightState"
  }
  object RedLight extends LightState("Red", "R")
  object GreenLight extends LightState("Green", "G")
  object OrangeLight extends LightState("Orange", "O")

  case class LightColorMessage(state: LightState)

}