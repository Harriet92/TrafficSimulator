trait MapLoader {
  def loadMap: Map[(Int, Int), RoadDirection]
}

object MapLoader {

  case class MapLoadedMessage(map: Map[(Int, Int), RoadDirection])
  case object LoadMapCommand

  def apply(): MapLoader = new DumbMapLoader()
  def fileLoader(filename: String) = new FileMapLoader(filename)
}





