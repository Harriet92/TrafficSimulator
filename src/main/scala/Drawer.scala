import java.awt.{Color, Dimension}
import javax.swing.{JFrame, SwingUtilities}

import akka.actor.{ActorRef, ActorLogging, Actor}

class Drawer() extends Actor with ActorLogging {

  var mapPanel: MapPanel = new MapPanel(null)
  val mainFrame = new JFrame {
    setMinimumSize(new Dimension(Consts.windowWidth, Consts.windowHeight))
    setPreferredSize(new Dimension(Consts.windowWidth, Consts.windowHeight))
  }

  def receive = {
    case Master.DrawMap(map, crossings) => {
      drawMap(map, crossings)
      sender ! Drawer.InitializationFinished()
    }
    case Master.RefreshCars(cars) => mapPanel.refreshCars(cars)
    case Crossing.TrafficLightsChanged(state) => mapPanel.changeLightsColor(state, sender())
    case _ =>
  }

  def configureMainFrame() {
    mainFrame.setTitle("Traffic simulator")
    mainFrame.setBackground(Color.LIGHT_GRAY)
    mainFrame.getContentPane.add(mapPanel)
    mainFrame.setLocationRelativeTo(null)
  }

  def drawMap( map: Map[Location, RoadDirection], crossings: Map[Location, ActorRef] ) {
    mapPanel = new MapPanel(map)
    mapPanel.crossingsMapInit(crossings)
    configureMainFrame()
    SwingUtilities.invokeLater(new Runnable {
      def run {
        mainFrame.setVisible(true)
      }
    })
  }
}

object Drawer{
  case class InitializationFinished()
}