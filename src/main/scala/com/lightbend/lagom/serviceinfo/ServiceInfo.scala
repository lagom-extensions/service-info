package com.lightbend.lagom.serviceinfo

import com.github.ghik.silencer.silent
import com.lightbend.lagom.buildinfo.LagomBuildInfo
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import play.api.libs.json.{Format, JsNull, JsValue, Json}

import scala.collection.JavaConverters._

case class ServiceInfo(service: String, version: String, payload: Map[String, String])
object ServiceInfo {
  implicit val format: Format[ServiceInfo] = Json.format
  implicit val buildInfoToServiceInfo: LagomBuildInfo => ServiceInfo = { buildInfo: LagomBuildInfo =>
    val payload = buildInfo.toMap.filter { case (_, v) => v.isInstanceOf[String] }.mapValues(v => v.toString)
    ServiceInfo(service = buildInfo.service, version = buildInfo.version, payload = payload)
  }
}

case class ServiceHealthInfo(service: String, statusCode: String, details: JsValue = JsNull)
object ServiceHealthInfo {
  implicit val format: Format[ServiceHealthInfo] = Json.format
}
case class HealthInfo(statusCode: String, details: JsValue = JsNull)
@silent
object HealthInfo {
  val applicationName = "app"
  val simpleParam = "simple"
  private[this] val log = LoggerFactory.getLogger(getClass)

  implicit val format: Format[HealthInfo] = Json.format
  implicit val toHealthInfo: Health => HealthInfo = { health: Health =>
    val (details, ignoredDetails) = health.getDetails.asScala.partition {
      case (_, v) => v.isInstanceOf[Health]
    }
    if (ignoredDetails.nonEmpty) log.error(s"Health details ignored for types: ${ignoredDetails.values.toSet}")

    val detailsJson = Json.toJson(
      details
        .mapValues(_.asInstanceOf[Health])
        .mapValues(toHealthInfo)
        .mapValues(v => Json.toJson(v))
    )
    HealthInfo(health.getStatus.getCode, detailsJson)
  }
}
