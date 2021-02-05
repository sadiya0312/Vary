package varys.util

import varys.Logging

import java.io.{IOException, InputStream}

private[varys] class ThrottledInputStream(
                                           val rawStream: InputStream,
                                           val ownerName: String,
                                           val initBitPerSec: Double = 0.0)
  extends InputStream() with Logging  {
  val startTime: Long = System.currentTimeMillis()

  val mBPSLock = new Object

  var maxBytesPerSec: Long = (initBitPerSec / 8).toLong
  var bytesRead = 0L
  var totalSleepTime = 0L

  val SLEEP_DURATION_MS = 50L

  if (maxBytesPerSec < 0) {
    throw new IOException("Bandwidth " + maxBytesPerSec + " is invalid")
  }

  override def read(): Int = {
    throttle()
    val data = rawStream.read()
    if (data != -1) {
      bytesRead += 1
    }
    data
  }

  override def read(b: Array[Byte]): Int = {
    throttle()
    val readLen = rawStream.read(b)
    if (readLen != -1) {
      bytesRead += readLen
    }
    readLen
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    throttle()
    val readLen = rawStream.read(b, off, len)
    if (readLen != -1) {
      bytesRead += readLen
    }
    readLen
  }

  private def throttle() {
    while (maxBytesPerSec <= 0.0) {
      mBPSLock.synchronized {
        logTrace(this + " maxBytesPerSec <= 0.0. Sleeping.")
        mBPSLock.wait()
      }
    }

    // NEVER exceed the specified rate
    while (getBytesPerSec > maxBytesPerSec) {
      try {
        Thread.sleep(SLEEP_DURATION_MS)
        totalSleepTime += SLEEP_DURATION_MS
      } catch {
        case ie: InterruptedException => throw new IOException("Thread aborted", ie)
      }
    }
  }

  def setNewRate(newMaxBitPerSec: Double) {
    maxBytesPerSec = (newMaxBitPerSec / 8).toLong
    mBPSLock.synchronized {
      logTrace(this + " newMaxBitPerSec = " + newMaxBitPerSec)
      mBPSLock.notifyAll()
    }
  }

  def getTotalBytesRead: Long = bytesRead

  def getBytesPerSec: Long = {
    val elapsed = (System.currentTimeMillis() - startTime) / 1000
    if (elapsed == 0) {
      bytesRead
    } else {
      bytesRead / elapsed
    }
  }

  def getTotalSleepTime: Long = totalSleepTime

  override def toString: String = {
    "ThrottledInputStream{" +
      "ownerName=" + ownerName +
      ", bytesRead=" + bytesRead +
      ", maxBytesPerSec=" + maxBytesPerSec +
      ", bytesPerSec=" + getBytesPerSec +
      ", totalSleepTime=" + totalSleepTime +
      '}';
  }
}
