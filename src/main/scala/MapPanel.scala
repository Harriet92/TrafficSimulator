import javax.swing.JPanel
import java.awt.{Color, Graphics}

import akka.actor.ActorRef

import scala.collection.mutable

class MapPanel(var map : Map[Location, RoadDirection]) extends JPanel {

  val panelWidth = 600
  val panelHeight = 400
  val tileSize = 40
  var carSize = tileSize / 2
  var currentX = 0
  var currentY = 0
  val roadColor = Color.GRAY
  val crossColor = Color.BLUE
  val grassColor = Color.GREEN
  val carColor = Color.ORANGE
  var cars : mutable.Map[ActorRef, Location] = null

  def refreshCars(_cars: mutable.Map[ActorRef, Location]): Unit = {
    cars = _cars
    repaint()
  }

  override def paintComponent(g: Graphics) {
    g.clearRect(0, 0, panelWidth, panelHeight)

    g.setColor(roadColor)
    if(map!=null) {
      for ((k, v) <- map) {
        g.fillRect(k.x * tileSize, k.y * tileSize, tileSize, tileSize)
      }
    }

    g.setColor(carColor)
    if(cars!=null) {
      for ((k, v) <- cars) {
        g.fillRect(v.x * tileSize + (tileSize - carSize)/2, v.y * tileSize + (tileSize - carSize)/2, carSize, carSize)
      }
    }
  }
}