package com.lightbend.lagom.serviceinfo

import akka.NotUsed
import com.lightbend.lagom.buildinfo.LagomBuildInfo
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.serviceinfo.ServiceInfo.buildInfoToServiceInfo

import scala.concurrent.Future

trait LagomServiceInfoAutoService {
  this: LagomServiceInfoService =>
  def buildInfo: LagomBuildInfo
  override def getServiceInfo: ServiceCall[NotUsed, ServiceInfo] = ServiceCall { _ =>
    import scala.concurrent.ExecutionContext.Implicits.global
    Future(buildInfoToServiceInfo(buildInfo))
  }
}
