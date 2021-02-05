package varys.framework.master.scheduler

import varys.Logging
import varys.framework.master.{CoflowInfo, FlowInfo}

import scala.collection.mutable.ArrayBuffer

/**
 * Implementation of the Shortest-Effective-Bottleneck-First coflow scheduler.
 * It sorts coflows by expected CCT based on current bottlebneck duration.
 * All coflows are accepted; hence, there is no admission control.
 */
class SEBFScheduler extends OrderingBasedScheduler with Logging{
  override def getOrderedCoflows(
                                  activeCoflows: ArrayBuffer[CoflowInfo]): ArrayBuffer[CoflowInfo] = {
    activeCoflows.sortWith(_.calcAlpha < _.calcAlpha)
  }

  override def calcFlowRate(
                             flowInfo: FlowInfo,
                             cf: CoflowInfo,
                             minFree: Double): Double = {

    minFree * (flowInfo.getFlowSize() / cf.origAlpha)
  }
}
