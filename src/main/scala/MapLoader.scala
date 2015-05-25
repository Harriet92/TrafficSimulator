import akka.actor.{ActorRef, ActorContext}

trait MapLoader {
  def loadMap(context: ActorContext, drawer: ActorRef): (Map[Location, RoadDirection], Map[Location, ActorRef])
}

object MapLoader {
  def apply(): MapLoader = new DumbMapLoader()
  def fileLoader(filename: String) = new FileMapLoader(filename)
}





