package com.lightbend.lagom.serviceinfo

import scala.concurrent.{ExecutionContext, Future}

trait LagomServiceAggreage {
  def toServiceHealthInfo(service: String, healthInfo: HealthInfo): ServiceHealthInfo = {
    ServiceHealthInfo(service, healthInfo.statusCode, healthInfo.details)
  }
  def servicesHealthInfo(serviceInfoServices: Seq[LagomServiceInfoService], isSimpleCheck: Boolean)(implicit ex: ExecutionContext): Seq[Future[ServiceHealthInfo]] =
    serviceInfoServices.map(
      s =>
        s.healthInfo(isSimpleCheck)
          .invoke()
          .map(healthInfo => toServiceHealthInfo(s.descriptor.name, healthInfo))
          .recover { case _ => ServiceHealthInfo(s.descriptor.name, "DOWN") }
    )
}
