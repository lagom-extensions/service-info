package com.lightbend.lagom.serviceinfo

import com.lightbend.lagom.buildinfo.LagomBuildInfo
import play.api.libs.json.{Format, Json}

case class ServiceInfo(service: String, version: String, payload: Map[String, String])
object ServiceInfo {
  implicit val format: Format[ServiceInfo] = Json.format
  def buildInfoToServiceInfo(buildInfo: LagomBuildInfo): ServiceInfo = {
    val payload = buildInfo.toMap.filter { case (_, v) => v.isInstanceOf[String] }.mapValues(v => v.toString)
    ServiceInfo(service = buildInfo.service, version = buildInfo.version, payload = payload)
  }
}
