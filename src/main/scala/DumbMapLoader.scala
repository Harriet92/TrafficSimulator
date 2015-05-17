import akka.actor.{ActorRef, ActorContext}

class DumbMapLoader extends MapLoader {
  override def loadMap(context: ActorContext): (Map[(Int, Int), RoadDirection], Map[(Int, Int), ActorRef]) = {
    println("Loading map!")

    (Map((1, 2) -> RoadDirection(left = true, right = true, top = true, bottom = true)), Map[(Int, Int), ActorRef]())
  }
}