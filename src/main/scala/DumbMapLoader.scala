class DumbMapLoader extends MapLoader {

  override def loadMap: Map[(Int, Int), RoadDirection] = {
    println("Loading map!")

    Map((1, 2) -> RoadDirection(left = true, right = true, top = true, bottom = true))
  }

}