import akka.actor.{ActorSystem, Props}

object TrafficSimulator extends App {

  val system = ActorSystem("traffic-simulator")
  val drawer = system.actorOf(Props[DrawerActor], name = "drawer")
  val master = system.actorOf(Props(new Master(drawer)), "master")
  system.awaitTermination()
}