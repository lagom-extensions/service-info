package com.lightbend.lagom.serviceinfo

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service.{named, pathCall}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait LagomServiceInfoService {
  this: Service =>
  def serviceName: String
  def healthInfo(simple: Boolean = true): ServiceCall[NotUsed, HealthInfo]
  def getServiceInfo: ServiceCall[NotUsed, ServiceInfo]

  final def descriptor: Descriptor = {
    applyDescriptorCalls(
      named(serviceName)
        .withCalls(
          pathCall("/actuator/health?simple", healthInfo _),
          pathCall("/actuator/service-info", getServiceInfo))
    )
  }

  def applyDescriptorCalls(descriptor: Descriptor): Descriptor = descriptor
}