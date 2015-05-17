import akka.actor.{Props, ActorContext, ActorRef}

import scala.io.Source

class FileMapLoader(file: String) extends MapLoader {

  override def loadMap(context: ActorContext): (Map[(Int, Int), RoadDirection], Map[(Int, Int), ActorRef]) = {
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

      (finalTile, index) <- toDirection(left, right, top, bottom, context).view.zipWithIndex

    } yield (row * 2 + index / 2, column * 2 + index % 2) -> finalTile

    (finalDirections.map(tuple => tuple._1 -> tuple._2._1).toMap, finalDirections.map(tuple => tuple._1 -> tuple._2._2).filter(_._2 != None).map(tuple => tuple._1 -> tuple._2.get).toMap)
  }

  def toDirection(left: Boolean, right: Boolean, top: Boolean, bottom: Boolean, context: ActorContext): List[(RoadDirection, Option[ActorRef])] = {
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

    var directions = 0
    if (left) directions += 1
    if (right) directions += 1
    if (top) directions += 1
    if (bottom) directions += 1

    val crossing = if(directions > 2) Some(context.actorOf(Props(new Crossing(new Crossing.Options())))) else None
    List((leftTop, crossing), (rightTop, crossing), (leftBottom, crossing), (rightBottom, crossing))
  }
}
