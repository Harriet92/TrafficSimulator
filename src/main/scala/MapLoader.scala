import akka.actor.{ActorRef, ActorContext}

trait MapLoader {
  def loadMap(context: ActorContext): (Map[(Int, Int), RoadDirection], Map[(Int, Int), ActorRef])
}

object MapLoader {

  case class MapLoadedMessage(map: Map[(Int, Int), RoadDirection])
  case object LoadMapCommand

  def apply(): MapLoader = new DumbMapLoader()
  def fileLoader(filename: String) = new FileMapLoader(filename)
}





