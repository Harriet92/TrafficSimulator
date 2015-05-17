import akka.actor.{ActorSystem, Props}

object TrafficSimulator extends App {
  val system = ActorSystem("traffic-simulator")

  val master = system.actorOf(Props(new Master), "master")

  val firstCar = system.actorOf(Props(new Car(4, 6, 4, 2, master)), "carrrr")

  firstCar.tell(Car.MoveFinished, firstCar)

  system.awaitTermination()
}