/**
 * Created by Emilka on 2015-05-14.
 */

import akka.actor.{ActorSystem, ActorLogging, Actor, Props}

case object Ticket
case object FullPint
case object EmptyPint

class BarTender extends Actor with ActorLogging {
  def receive = {
    case Ticket =>
      log.info("1 pint coming right up")

      Thread.sleep(1000)

      log.info("Your pint is ready, here you go")

      sender ! FullPint

    case EmptyPint =>
      log.info("I think you're done for the day")

      context.system.shutdown()
  }
}

class Person extends Actor with ActorLogging {
  def receive = {
    case FullPint =>
      log.info("I'll make short work of this")

      Thread.sleep(1000)

      log.info("I'm ready for the next")

      sender ! EmptyPint
  }
}

object HowdyAkka extends App {
  val system = ActorSystem("howdy-akka")

  val zed = system.actorOf(Props(new BarTender), "zed")

  val alice = system.actorOf(Props(new Person), "alice")

  zed.tell(Ticket, alice)

  system.awaitTermination()
}