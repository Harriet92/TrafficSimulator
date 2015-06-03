import scala.concurrent.duration._

object Consts {
  val tileSize = 18
  val mapColumnsCount = 20 * 2
  val mapRowsCount = 20 * 2
  val carsCount = 50
  val windowWidth = mapColumnsCount * tileSize
  val windowHeight = mapRowsCount * tileSize
  val lightSize = 15
  val carVelocity = 1 seconds
  val mapFilename = "final_map.txt"
}
