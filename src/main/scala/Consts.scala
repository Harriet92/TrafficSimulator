import scala.concurrent.duration._

object Consts {
  val tileSize = 16
  val mapColumnsCount = 37 * 2
  val mapRowsCount = 33 * 2
  val carsCount = 200
  val windowWidth = mapColumnsCount * tileSize
  val windowHeight = mapRowsCount * tileSize
  val lightSize = 15
  val carVelocity = 1 seconds
  val waitingSchedulerSpan = 3 seconds
  val mapFilename = "big_map.txt"
}
