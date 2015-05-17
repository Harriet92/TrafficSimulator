trait MapLoader {
  def loadMap: Map[(Int, Int), RoadDirection]
}

object MapLoader {

  case class MapLoadedMessage(map: Map[(Int, Int), RoadDirection])
  case object LoadMapCommand

  def apply(): MapLoader = new DumbMapLoader()
  def fileLoader(filename: String) = new FileMapLoader(filename)
}

case class RoadDirection(left: Boolean, right: Boolean, top: Boolean, bottom: Boolean) {
  def +(dir: RoadDirection): RoadDirection = {
    new RoadDirection(left || dir.left, right || dir.right, top || dir.top, bottom || dir.bottom)
  }
}

object LeftDirection extends RoadDirection(true, false, false, false)
object RightDirection extends RoadDirection(false, true, false, false)
object TopDirection extends RoadDirection(false, false, true, false)
object BottomDirection extends RoadDirection(false, false, false, true)




