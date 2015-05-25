import akka.actor.{ActorRef, ActorContext}

class DumbMapLoader extends MapLoader {
  override def loadMap(context: ActorContext, drawer: ActorRef): (Map[Location, RoadDirection], Map[Location, ActorRef]) = {
    println("Loading map!")

    (Map(new Location(1, 2) -> RoadDirection(left = true, right = true, top = true, bottom = true)), Map[Location, ActorRef]())
  }
}