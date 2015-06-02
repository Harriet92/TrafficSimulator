import java.io.File
import javax.imageio.ImageIO
import javax.swing.JPanel
import java.awt.{Color, Graphics}

import akka.actor.ActorRef

import scala.collection.mutable

class MapPanel(var map : Map[Location, RoadDirection]) extends JPanel {

  val panelWidth = Consts.windowWidth
  val panelHeight = Consts.windowHeight
  val tileSize = Consts.tileSize
  var carSize = tileSize / 2
  val lightSize = Consts.lightSize
  var currentX = 0
  var currentY = 0
  val roadColor = Color.DARK_GRAY
  val crossColor = Color.BLUE
  val grassColor = Color.LIGHT_GRAY
  val carColor = Color.BLACK
  val inactiveCarColor = Color.YELLOW
  var cars : mutable.Map[ActorRef, Location] = null
  var inactiveCars : mutable.Map[ActorRef, Location] = null
  var crossToColor = mutable.Map[(Location, ActorRef), Color]()
  val grassImage = ImageIO.read(new File("src/main/resources/grass.png"))
  val ballImage = ImageIO.read(new File("src/main/resources/ball.png"))
  val ballYellowImage = ImageIO.read(new File("src/main/resources/ball_yellow.png"))
  val rockImage = ImageIO.read(new File("src/main/resources/rock.png"))

  def refreshCars(_cars: mutable.Map[ActorRef, Location]): Unit = {
    cars = _cars
  }

  def refreshInactiveCars(_cars: mutable.Map[ActorRef, Location]): Unit = {
    inactiveCars = _cars
  }

  def crossingsMapInit(crossings: Map[Location, ActorRef]): Unit ={
    for(pair <- crossings){
      crossToColor+= (pair -> crossColor)
    }
  }

  def changeLightsColor(state: Crossing.CurrentState, crossing: ActorRef): Unit ={
    var temp = mutable.Map[Location, ActorRef]()
    for (((k, v),c) <- crossToColor) {
      if(v == crossing)
        temp += (k -> v)
    }
    val minX = temp.keys.toList.sortBy(loc => loc.x).head.x
    val minY = temp.keys.toList.sortBy(loc => loc.y).head.y
    val maxX = temp.keys.toList.sortBy(loc => -loc.x).head.x
    val maxY = temp.keys.toList.sortBy(loc => -loc.y).head.y

    crossToColor((new Location(minX, minY), crossing)) = Crossing.lightStateToColor(state.vstate)
    crossToColor((new Location(maxX, maxY), crossing)) = Crossing.lightStateToColor(state.vstate)
    crossToColor((new Location(minX, maxY), crossing)) = Crossing.lightStateToColor(state.hstate)
    crossToColor((new Location(maxX, minY), crossing)) = Crossing.lightStateToColor(state.hstate)
  }


  override def paintComponent(g: Graphics) {
    g.clearRect(0, 0, panelWidth, panelHeight)
    g.drawImage(grassImage,0,0,Consts.windowWidth, Consts.windowHeight, null)
    //g.setColor(roadColor)
    if(map!=null) {
      for ((k, v) <- map) {
        g.drawImage(rockImage, k.x * tileSize, k.y * tileSize, tileSize, tileSize, Color.GRAY, null)
      }
    }

    if(crossToColor!=null) {
      for (((k, v),c) <- crossToColor) {
        g.setColor(c)
        g.fillOval(k.x * tileSize + (tileSize - lightSize)/2, k.y * tileSize + (tileSize - lightSize)/2, lightSize, lightSize)
      }
    }

    if(inactiveCars!=null) {
      for ((k, v) <- inactiveCars) {
        g.drawImage(ballYellowImage, v.x * tileSize + (tileSize - carSize)/2, v.y * tileSize + (tileSize - carSize)/2, carSize, carSize, null)
      }
    }

    g.setColor(carColor)
    if(cars!=null) {
      for ((k, v) <- cars) {
        g.drawImage(ballImage, v.x * tileSize + (tileSize - carSize)/2, v.y * tileSize + (tileSize - carSize)/2, carSize, carSize, null)
      }
    }
  }
}