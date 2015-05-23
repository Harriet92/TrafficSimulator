
case class RoadDirection(left: Boolean, right: Boolean, top: Boolean, bottom: Boolean) {
  def +(dir: RoadDirection): RoadDirection = {
    new RoadDirection(left || dir.left, right || dir.right, top || dir.top, bottom || dir.bottom)
  }

  def -(dir: RoadDirection): RoadDirection = {
    new RoadDirection(left && !dir.left, right && !dir.right, top && !dir.top, bottom && !dir.bottom)
  }

  def contains(dir: RoadDirection): Boolean =
    (this.top || !dir.top) && (this.left || !dir.left) && (this.right || !dir.right) && (this.bottom || !dir.bottom)

  def reverse(): RoadDirection =
    new RoadDirection(right, left, bottom, top)

  def applyMovement(loc: Location): Location = {
    var newLoc = loc

    if(left) {
      newLoc = new Location(newLoc.x - 1, newLoc.y)
    }
    if(right) {
      newLoc = new Location(newLoc.x + 1, newLoc.y)
    }
    if(top) {
      newLoc = new Location(newLoc.x, newLoc.y - 1)
    }
    if(bottom) {
      newLoc = new Location(newLoc.x, newLoc.y + 1)
    }

    newLoc
  }

  override def toString: String = {
    "(" + (if(left) "Left" else "") + (if(right) " Right" else "") + (if(top) " Top" else "") + (if(bottom) " Bottom" else "") + ")"
  }
}

object NoDirection extends RoadDirection(false, false, false, false)
object LeftDirection extends RoadDirection(true, false, false, false)
object RightDirection extends RoadDirection(false, true, false, false)
object TopDirection extends RoadDirection(false, false, true, false)
object BottomDirection extends RoadDirection(false, false, false, true)
object AllDirections extends RoadDirection(true, true, true, true)