package varys.framework.master.ui

import akka.actor.ActorRef
import jakarta.servlet.http.HttpServletRequest
import org.eclipse.jetty.server.{Handler, Server}
import varys.{Logging, Utils}
import varys.ui.JettyUtils
import varys.ui.JettyUtils._

import scala.concurrent.duration.Duration

private[varys] class MasterWebUI(masterActorRef_ : ActorRef, requestedPort: Int) extends Logging{

  implicit val timeout = Duration.create(
    System.getProperty("varys.akka.askTimeout", "10").toLong, "seconds")
  val host = Utils.localHostName()
  val port = requestedPort

  val masterActorRef = masterActorRef_

  var server: Option[Server] = None
  var boundPort: Option[Int] = None

  val coflowPage = new CoflowPage(this)
  val indexPage = new IndexPage(this)

  def start() {
    try {
      val (srv, bPort) = JettyUtils.startJettyServer("0.0.0.0", port, handlers)
      server = Some(srv)
      boundPort = Some(bPort)
      logInfo("Started Master web UI at http://%s:%d".format(host, boundPort.get))
    } catch {
      case e: Exception =>
        logError("Failed to create Master JettyUtils", e)
        System.exit(1)
    }
  }

  val handlers = Array[(String, Handler)](
    ("/static", createStaticHandler(MasterWebUI.STATIC_RESOURCE_DIR)),
    ("/coflow/json", (request: HttpServletRequest) => coflowPage renderJson request),
    ("/coflow", (request: HttpServletRequest) => coflowPage render request),
    ("/json", (request: HttpServletRequest) => indexPage renderJson request),
    ("*", (request: HttpServletRequest) => indexPage render request)
  )

  def stop() {
    server.foreach(_.stop())
  }

}

private[varys] object MasterWebUI {
  val STATIC_RESOURCE_DIR = "varys/ui/static"
}