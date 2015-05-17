
case class RoadDirection(left: Boolean, right: Boolean, top: Boolean, bottom: Boolean) {
  def +(dir: RoadDirection): RoadDirection = {
    new RoadDirection(left || dir.left, right || dir.right, top || dir.top, bottom || dir.bottom)
  }

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
}

object LeftDirection extends RoadDirection(true, false, false, false)
object RightDirection extends RoadDirection(false, true, false, false)
object TopDirection extends RoadDirection(false, false, true, false)
object BottomDirection extends RoadDirection(false, false, false, true)