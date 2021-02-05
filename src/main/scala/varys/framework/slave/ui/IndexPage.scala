package varys.framework.slave.ui

import akka.pattern.ask
import jakarta.servlet.http.HttpServletRequest
import net.liftweb.json.JsonAST.JValue
import varys.framework.{JsonProtocol, RequestSlaveState, SlaveState}
import varys.ui.UIUtils

import scala.xml.Node
import scala.concurrent.Await
import scala.concurrent.duration._
import varys.framework.JsonProtocol
import varys.framework.{RequestSlaveState, SlaveState}
import varys.ui.UIUtils
import varys.Utils

private[varys] class IndexPage(parent: SlaveWebUI) {
  val slaveActor = parent.slave.self
  val slave = parent.slave
  val timeout = parent.timeout

  def renderJson(request: HttpServletRequest): JValue = {
    val stateFuture = (slaveActor ? RequestSlaveState)(timeout).mapTo[SlaveState]
    val slaveState = Await.result(stateFuture, 30.seconds)
    JsonProtocol.writeSlaveState(slaveState)
  }

  def render(request: HttpServletRequest): Seq[Node] = {
    val stateFuture = (slaveActor ? RequestSlaveState)(timeout).mapTo[SlaveState]
    val slaveState = Await.result(stateFuture, 30.seconds)

    val content =
      <div class="row-fluid"> <!-- Slave Details -->
        <div class="span12">
          <ul class="unstyled">
            <li><strong>ID:</strong> {slaveState.slaveId}</li>
            <li><strong>
              Master URL:</strong> {slaveState.masterUrl}
            </li>
            <li><strong>RxBps:</strong> {slaveState.rxBps}</li>
            <li><strong>TxBps:</strong> {slaveState.txBps}</li>
          </ul>
          <p><a href={slaveState.masterWebUiUrl}>Back to Master</a></p>
        </div>
      </div>

    UIUtils.basicVarysPage(content, "Varys Slave at %s:%s".format(
      slaveState.host, slaveState.port))
  }

}
