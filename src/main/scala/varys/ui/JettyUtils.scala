package varys.ui

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import net.liftweb.json.DefaultFormats
import net.liftweb.json.JsonAST.{JValue, prettyRender}
import org.eclipse.jetty.server.handler.{AbstractHandler, ContextHandler, HandlerList, ResourceHandler}
import org.eclipse.jetty.server.{Handler, Request, Server, ServerConnector}
import org.eclipse.jetty.util.thread.QueuedThreadPool
import varys.Logging

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}
import scala.xml.Node

/** Utilities for launching a web server using Jetty's HTTP Server class */
private[varys] object JettyUtils extends Logging{

  implicit val formats: DefaultFormats.type = net.liftweb.json.DefaultFormats
  // Base type for a function that returns something based on an HTTP request. Allows for
  // implicit conversion from many types of functions to jetty Handlers.
  type Responder[T] = HttpServletRequest => T

  implicit def jValueToAnyRef(value: JValue): AnyRef = value.asInstanceOf[AnyRef]
  implicit def sequenceNodeToAnyRef(value: Seq[Node]): AnyRef = value.asInstanceOf[AnyRef]

  // Conversions from various types of Responder's to jetty Handlers
  implicit def jsonResponderToHandler(responder: Responder[JValue]) : Handler =
  createHandler(responder, "text/json", (in: JValue) => prettyRender(in))

  implicit def htmlResponderToHandler(responder: Responder[Seq[Node]]): Handler =
    createHandler(responder, "text/html", (in: Seq[Node]) => "<!DOCTYPE html>" + in.toString)

  implicit def textResponderToHandler(responder: Responder[String]): Handler =
    createHandler(responder, "text/plain")

  def createHandler[T <% AnyRef](responder: Responder[T], contentType: String,
                                   extractFn: T => String = (in: Any) => in.toString): Handler ={
    new AbstractHandler {
      override def handle(target: String,
                          baseRequest: Request,
                          request: HttpServletRequest,
                          response: HttpServletResponse): Unit = {
        response.setContentType("%s;charset=utf-8".format(contentType))
        response.setStatus(HttpServletResponse.SC_OK)
        baseRequest.setHandled(true)
        val result = responder(request)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.getWriter.println(extractFn(result))
      }
    }
  }

  /** Creates a handler that always redirects the user to a given path */
  def createRedirectHandler(newPath: String) : Handler = {
    new AbstractHandler {
      override def handle(target: String,
                          baseRequest: Request,
                          request: HttpServletRequest,
                          response: HttpServletResponse): Unit ={
        response.setStatus(302)
        response.setHeader("Location", baseRequest.getRootURL + newPath)
        baseRequest.setHandled(true)
      }
    }
  }

  /** Creates a handler for serving files from a static directory */
  def createStaticHandler(resourceBase: String) : ResourceHandler = {
    val staticHandler = new ResourceHandler
    Option(getClass.getClassLoader.getResource(resourceBase)) match {
      case Some(res) =>
        staticHandler.setResourceBase(res.toString)
      case None =>
        throw new Exception("Could not find resource path for Web UI: " + resourceBase)
    }

    staticHandler
  }

  /**
   * Attempts to start a Jetty server at the supplied ip:port which uses the supplied handlers.
   *
   * If the desired port number is contented, continues incrementing ports until a free port is
   * found. Returns the chosen port and the jetty Server object.
   */
  def startJettyServer(ip: String, port: Int, handlers: Seq[(String, Handler)]): (Server,Int) = {
    val handlersToRegister = handlers.map { case (path, handler) =>
      val contextHandler = new ContextHandler(path)
      contextHandler.setHandler(handler)
      contextHandler.asInstanceOf[Handler]
    }

    val  handlerList = new HandlerList
    handlerList.setHandlers(handlersToRegister.toArray)

    @tailrec
    def connect(currentPort: Int): (Server, Int) = {
      val pool = new QueuedThreadPool
      pool.setDaemon(true)
      val server = new Server(pool)
      server.setHandler(handlerList)
      val connector = new ServerConnector(server)
      connector.setHost(ip)
      connector.setPort(currentPort)
      server.addConnector(connector)

      Try {server.start()} match {
        case s: Success[_] =>
          (server, server.getConnectors.head.asInstanceOf[ServerConnector].getLocalPort)
        case f: Failure[_] =>
          server.stop()
          logInfo("Failed to create UI at port, %s. Trying again.".format(currentPort))
          logInfo("Error was: " + f.toString)
          connect((currentPort+1) % 65536)
      }

    }
    connect(port)
  }
}
