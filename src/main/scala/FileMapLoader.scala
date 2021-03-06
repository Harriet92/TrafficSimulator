import akka.actor.{Props, ActorContext, ActorRef}

import scala.collection.immutable.HashSet
import scala.io.Source

class FileMapLoader(file: String) extends MapLoader {

  private val toDirections = Map[RoadDirection, List[RoadDirection]](
    NoDirection -> List(BottomDirection, LeftDirection, RightDirection, TopDirection),

    LeftDirection -> List(LeftDirection, LeftDirection, RightDirection, TopDirection),
    RightDirection -> List(BottomDirection, LeftDirection, RightDirection, RightDirection),
    TopDirection -> List(BottomDirection, TopDirection, RightDirection, TopDirection),
    BottomDirection -> List(BottomDirection, LeftDirection, BottomDirection, TopDirection),

    LeftDirection + RightDirection -> List(LeftDirection, LeftDirection, RightDirection, RightDirection),
    LeftDirection + BottomDirection -> List(LeftDirection, LeftDirection, BottomDirection, TopDirection),
    LeftDirection + TopDirection -> List(LeftDirection, TopDirection, RightDirection, TopDirection),
    RightDirection + BottomDirection -> List(BottomDirection, LeftDirection, BottomDirection, RightDirection),
    RightDirection + TopDirection -> List(BottomDirection, TopDirection, RightDirection, RightDirection),
    TopDirection + BottomDirection -> List(BottomDirection, TopDirection, BottomDirection, TopDirection),

    AllDirections - RightDirection -> List(LeftDirection + BottomDirection, LeftDirection + TopDirection,
      BottomDirection + RightDirection, TopDirection),
    AllDirections - LeftDirection -> List(BottomDirection, LeftDirection + TopDirection,
      BottomDirection + RightDirection, TopDirection + RightDirection),
    AllDirections - BottomDirection -> List(LeftDirection + BottomDirection, LeftDirection + TopDirection,
      RightDirection, TopDirection + RightDirection),
    AllDirections - TopDirection -> List(LeftDirection + BottomDirection, LeftDirection,
      RightDirection + BottomDirection, TopDirection + RightDirection),

    AllDirections -> List(LeftDirection + BottomDirection, LeftDirection + TopDirection,
      RightDirection + BottomDirection, TopDirection + RightDirection)
  )

  override def loadMap(context: ActorContext, drawer: ActorRef): (Map[Location, RoadDirection], Map[Location, ActorRef]) = {
    val roadPositions = LoadFromFileToMap('o')

    val (width, heigth) = (Consts.mapColumnsCount / 2, Consts.mapRowsCount / 2)
    val finalDirections = for {
      column <- 0 to width
      row <- 0 to heigth if roadPositions.contains((column, row))

      left  = roadPositions.contains((column - 1, row))
      right = roadPositions.contains((column + 1, row))
      top = roadPositions.contains((column, row - 1))
      bottom = roadPositions.contains((column, row + 1))

      directions = new RoadDirection(left, right, top, bottom)
      crossing = createCrossing(directions, column, row, context, drawer)

      (finalTile, index) <- toDirections(directions).view.zipWithIndex

    } yield new Location(column * 2 + index % 2, row * 2 + index / 2) -> (finalTile, crossing)

    val roads = finalDirections.map(tuple => tuple._1 -> tuple._2._1).toMap
    val crossings = finalDirections.filter(_._2._2 != null).map(tuple => tuple._1 -> tuple._2._2).toMap
    (roads, crossings)
  }

  private def LoadFromFileToMap(char: Char): HashSet[(Int, Int)] = {
    val tuples = for {
      (line, row) <- Source.fromFile(file).getLines().toList.view.zipWithIndex
      (value, column) <- line.view.zipWithIndex if value == char
    } yield (column, row)

    HashSet[(Int, Int)](tuples: _*)
  }

  def createCrossing(direction: RoadDirection, x: Int, y: Int, context: ActorContext, drawer: ActorRef) : ActorRef = {
    var directions = 0
    if (direction.left) directions += 1
    if (direction.right) directions += 1
    if (direction.top) directions += 1
    if (direction.bottom) directions += 1

    if(directions > 2)
      context.actorOf(Props(new Crossing(new Crossing.Options(), drawer)), s"crossing$x,$y")
    else
      null
  }
}
