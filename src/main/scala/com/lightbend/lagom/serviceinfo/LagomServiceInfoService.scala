package com.lightbend.lagom.serviceinfo

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.Service.{named, pathCall}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait LagomServiceInfoService {
  this: Service =>
  def serviceName: String
  def getServiceInfo: ServiceCall[NotUsed, ServiceInfo]

  final def descriptor: Descriptor = {
    applyDescriptorCalls(
      named(serviceName)
        .withCalls(pathCall("/api/service-info", getServiceInfo))
    )
  }

  def applyDescriptorCalls(descriptor: Descriptor): Descriptor = descriptor
}
