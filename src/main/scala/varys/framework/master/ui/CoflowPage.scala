package varys.framework.master.ui

import akka.pattern.ask
import jakarta.servlet.http.HttpServletRequest
import net.liftweb.json.JsonAST.JValue
import varys.framework.{JsonProtocol, MasterState, RequestMasterState}
import varys.ui.UIUtils

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.xml.Node

private[varys] class CoflowPage(parent: MasterWebUI) {

  val master = parent.masterActorRef
  implicit val timeout = parent.timeout

  /** Details for a particular Coflow */
  def renderJson(request: HttpServletRequest): JValue = {
    val coflowId = request.getParameter("coflowId")
    val stateFuture = (master ? RequestMasterState)(timeout).mapTo[MasterState]
    val state = Await.result(stateFuture, 30.seconds)
    val coflow = state.activeCoflows.find(_.id == coflowId).getOrElse({
      state.completedCoflows.find(_.id == coflowId).getOrElse(null)
    })
    JsonProtocol.writeCoflowInfo(coflow)
  }

  /** Details for a particular Coflow */
  def render(request: HttpServletRequest): Seq[Node] = {
    val coflowId = request.getParameter("coflowId")
    val stateFuture = (master ? RequestMasterState)(timeout).mapTo[MasterState]
    val state = Await.result(stateFuture, 30.seconds)
    val coflow = state.activeCoflows.find(_.id == coflowId).getOrElse({
      state.completedCoflows.find(_.id == coflowId).getOrElse(null)
    })

    val content =
      <div class="row-fluid">
        <div class="span12">
          <ul class="unstyled">
            <li><strong>ID:</strong> {coflow.id}</li>
            <li><strong>Name:</strong> {coflow.desc.name}</li>
            <li><strong>User:</strong> {coflow.desc.user}</li>
            <li><strong>Submit Date:</strong> {coflow.submitDate}</li>
            <li><strong>State:</strong> {coflow.curState}</li>
          </ul>
        </div>
      </div>
    UIUtils.basicVarysPage(content, "Coflow: " + coflow.desc.name)
  }
}
