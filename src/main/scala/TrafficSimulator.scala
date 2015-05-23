import akka.actor.{ActorSystem, Props}

object TrafficSimulator extends App {
  val system = ActorSystem("traffic-simulator")

  val master = system.actorOf(Props(new Master), "master")

  system.awaitTermination()
}