
case class RoadDirection(left: Boolean, right: Boolean, top: Boolean, bottom: Boolean) {
  def +(dir: RoadDirection): RoadDirection = {
    new RoadDirection(left || dir.left, right || dir.right, top || dir.top, bottom || dir.bottom)
  }

  def contains(dir: RoadDirection): Boolean =
    (this.top || !dir.top) && (this.left || !dir.left) && (this.right || !dir.right) && (this.bottom || !dir.bottom)

  def reverse(): RoadDirection =
    new RoadDirection(right, left, bottom, top)

  def applyMovement(x :Int, y: Int): (Int, Int) = {
    var newX = x
    var newY = y

    if(left) {
      newX -= 1
    }
    if(right) {
      newX += 1
    }
    if(top) {
      newY -= 1
    }
    if(bottom) {
      newY += 1
    }

    (newX, newY)
  }

  override def toString: String = {
    "(" + (if(left) "Left" else "") + (if(right) " Right" else "") + (if(top) " Top" else "") + (if(bottom) " Bottom" else "") + ")"
  }
}

object LeftDirection extends RoadDirection(true, false, false, false)
object RightDirection extends RoadDirection(false, true, false, false)
object TopDirection extends RoadDirection(false, false, true, false)
object BottomDirection extends RoadDirection(false, false, false, true)