package varys.framework.client

import akka.actor.{ActorRef, ActorSystem}
import varys.framework.{DataIdentifier, DataServer}
import varys.util.ThrottledInputStream
import varys.{Logging, Utils}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.HashMap
import scala.concurrent.ExecutionContext

class VarysClient(
                 clientName: String,
                 masterUrl: String,
                 listener: ClientListener = null)
extends Logging{
  val INTERNAL_ASK_TIMEOUT_MS: Int =
    System.getProperty("varys.client.internalAskTimeoutMillis", "5000").toInt
  val RATE_UPDATE_FREQ = System.getProperty("varys.client.rateUpdateIntervalMillis", "100").toLong
  val SHORT_FLOW_BYTES = System.getProperty("varys.client.shortFlowMB", "0").toLong * 1048576
  val NIC_BPS = 1024 * 1048576

  var actorSystem: ActorSystem = null

  var masterActor: ActorRef = null
  val clientRegisterLock = new Object

  var slaveId: String = null
  var slaveUrl: String = null
  var slaveActor: ActorRef = null

  var clientId: String = null
  var clientActor: ActorRef = null

  // ExecutionContext for Futures
  implicit val futureExecContext = ExecutionContext.fromExecutor(Utils.newDaemonCachedThreadPool())

  var regStartTime = 0L

  val flowToTIS = new ConcurrentHashMap[DataIdentifier, ThrottledInputStream]()
  // TODO: Currently using flowToBitPerSec inside synchronized blocks. Might consider replacing with
  // an appropriate data structure; e.g., Collections.synchronizedMap.
  val flowToBitPerSec = new ConcurrentHashMap[DataIdentifier, Double]()
  val flowToObject = new HashMap[DataIdentifier, Array[Byte]]

  val serverThreadName = "ServerThread for Client@" + Utils.localHostName()
  var dataServer = new DataServer(0, serverThreadName, flowToObject)
  dataServer.start()

  var clientHost = Utils.localHostName()
  var clientCommPort = dataServer.getCommPort

}
