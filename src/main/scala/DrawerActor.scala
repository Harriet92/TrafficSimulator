import java.awt.{Color, Dimension}
import javax.swing.{JFrame, SwingUtilities}

import akka.actor.{ActorLogging, Actor}

class DrawerActor() extends Actor with ActorLogging {

  val WIDTH = 600
  val HEIGHT = 400
  var mapPanel: MapPanel = null
  val mainFrame = new JFrame {
    setMinimumSize(new Dimension(WIDTH, HEIGHT))
    setPreferredSize(new Dimension(WIDTH, HEIGHT))
  }

  def receive = {
    case Master.DrawMap(map) => drawMap(map)
    case Master.RefreshCars(cars) => mapPanel.refreshCars(cars)
    case _ =>
  }

  def configureMainFrame() {
    mainFrame.setTitle("Traffic simulator")
    mainFrame.setBackground(Color.GREEN)
    mainFrame.getContentPane.add(mapPanel)
    mainFrame.setLocationRelativeTo(null)
  }

  def drawMap( map: Map[Location, RoadDirection] ) {
    mapPanel = new MapPanel(map)
    configureMainFrame()
    SwingUtilities.invokeLater(new Runnable {
      def run {
        mainFrame.setVisible(true)
      }
    })
  }
}