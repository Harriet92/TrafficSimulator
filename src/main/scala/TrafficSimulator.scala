import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}


object TrafficSimulator extends App {
  val config: Config = ConfigFactory.parseString("""akka {
         loglevel = "ERROR"
         actor {
           debug {
             receive = on
             lifecycle = off
           }
         }
       }""").withFallback(ConfigFactory.load())
  val system = ActorSystem("traffic-simulator", config)
  val drawer = system.actorOf(Props[Drawer], name = "drawer")
  val master = system.actorOf(Props(new Master(drawer)), "master")
  system.awaitTermination()
}