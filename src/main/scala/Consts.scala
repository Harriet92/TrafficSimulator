import scala.concurrent.duration._

object Consts {
  val tileSize = 20
  val mapColumnsCount = 10 * 2
  val mapRowsCount = 10 * 2
  val carsCount = 20
  val windowWidth = mapColumnsCount * tileSize
  val windowHeight = mapRowsCount * tileSize
  val lightSize = 15
  val carVelocity = 1 seconds
  val mapFilename = "complex_map.txt"
}
