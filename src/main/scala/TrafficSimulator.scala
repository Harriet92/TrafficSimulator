import akka.actor.{ActorSystem, Props}

object TrafficSimulator extends App {
  val system = ActorSystem("traffic-simulator")

  val master = system.actorOf(Props(new Master), "master")

  val firstCar = system.actorOf(Props(new Car(0, 0, 0, 0, master)), "alice")
  val crossing = system.actorOf(Props(new Crossing), "crossing")

  firstCar.tell(Car.FieldQueryMessage(1, 1, 1, 1), master)

  system.awaitTermination()
}