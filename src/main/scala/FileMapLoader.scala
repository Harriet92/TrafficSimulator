import scala.io.Source

class FileMapLoader(file: String) extends MapLoader {

  override def loadMap: Map[(Int, Int), RoadDirection] = {
    val tuples = for {
      (line, row) <- Source.fromFile(file).getLines().toList.view.zipWithIndex
      (value, column) <- line.view.zipWithIndex
    } yield (row, column) -> (value != 'x')

    val map = tuples.toMap
    val ((width, heigth), _) = map.maxBy(x => x._1._1 + x._1._2)

    val finalDirections = for {
      column <- 0 to width
      row <- 0 to heigth

      mid = map.getOrElse((row, column), false) if mid
      left  = map.getOrElse((row, column - 1), false)
      right = map.getOrElse((row, column + 1), false)
      top = map.getOrElse((row - 1, column), false)
      bottom = map.getOrElse((row + 1, column), false)

      (finalTile, index) <- toDirection(left, right, top, bottom).view.zipWithIndex
    } yield (row * 2 + index / 2, column * 2 + index % 2) -> finalTile

    finalDirections.toMap
  }

  def toDirection(left: Boolean, right: Boolean, top: Boolean, bottom: Boolean): List[RoadDirection] = {
    var leftTop = new RoadDirection(false, false, false, false)
    var leftBottom = new RoadDirection(false, false, false, false)
    var rightTop = new RoadDirection(false, false, false, false)
    var rightBottom = new RoadDirection(false, false, false, false)

    if (left) {
      leftTop += LeftDirection; rightTop += LeftDirection
    }
    if (right) {
      leftBottom += RightDirection; rightBottom += RightDirection
    }
    if (top) {
      rightTop += TopDirection; rightBottom += TopDirection
    }
    if (bottom) {
      leftTop += BottomDirection; leftBottom += BottomDirection
    }

    List(leftTop, rightTop, leftBottom, rightBottom)
  }
}
