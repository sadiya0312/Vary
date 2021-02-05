package varys.ui.env

import jakarta.servlet.http.HttpServletRequest
import org.eclipse.jetty.server.Handler
import varys.ui.Page.Environment
import varys.ui.UIUtils

import scala.concurrent.JavaConversions
import scala.collection.convert.ImplicitConversions.`properties AsScalaMap`
import scala.util.Properties
import scala.xml.Node
import varys.ui.JettyUtils._

private[varys] class EnvironmentUI {
  def getHandlers: Seq[(String, Handler)] = Seq[(String, Handler)](
    ("/environment", envDetails(_))
  )

  def envDetails(request: HttpServletRequest): Seq[Node] = {
    val jvmInformation = Seq(
      ("Java Version", "%s (%s)".format(Properties.javaVersion, Properties.javaVendor)),
      ("Java Home", Properties.javaHome),
      ("Scala Version", Properties.versionString),
      ("Scala Home", Properties.scalaHome)
    ).sorted
    def jvmRow(kv: (String, String)) = <tr><td>{kv._1}</td><td>{kv._2}</td></tr>
    def jvmTable =
      UIUtils.listingTable(Seq("Name", "Value"), jvmRow, jvmInformation, fixedWidth = true)
    val properties = System.getProperties.iterator.toSeq
    val classPathProperty = properties.find { case (k, v) =>
      k.contains("java.class.path")
    }.getOrElse(("", ""))
    val varysProperties = properties.filter(_._1.startsWith("varys")).sorted
    val otherProperties = properties.diff(varysProperties :+ classPathProperty).sorted
    val propertyHeaders = Seq("Name", "Value")
    def propertyRow(kv: (String, String)) = <tr><td>{kv._1}</td><td>{kv._2}</td></tr>
    val varysPropertyTable =
      UIUtils.listingTable(propertyHeaders, propertyRow, varysProperties, fixedWidth = true)
    val otherPropertyTable =
      UIUtils.listingTable(propertyHeaders, propertyRow, otherProperties, fixedWidth = true)

    val content =
      <span>
        <h4>Runtime Information</h4> {jvmTable}
        <h4>Varys Properties</h4>
        {varysPropertyTable}
        <h4>System Properties</h4>
        {otherPropertyTable}
      </span>
    UIUtils.headerVarysPage(content, "Environment", Environment)
  }
}
