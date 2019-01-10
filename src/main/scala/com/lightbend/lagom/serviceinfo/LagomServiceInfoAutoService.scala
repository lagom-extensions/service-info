package com.lightbend.lagom.serviceinfo

import akka.NotUsed
import com.lightbend.lagom.buildinfo.LagomBuildInfo
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.serviceinfo.HealthInfo.applicationName
import com.lightbend.lagom.serviceinfo.LagomServiceInfoAutoService.aggregateHealthInfo
import org.springframework.boot.actuate.health.{Health, HealthAggregator, HealthIndicator, OrderedHealthAggregator}

import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

trait LagomServiceInfoAutoService { this: LagomServiceInfoService =>
  def buildInfo: LagomBuildInfo
  def healthIndicators: ListMap[String, HealthIndicator] = ListMap.empty[String, HealthIndicator]

  private val aggregator = new OrderedHealthAggregator()
  override def getServiceInfo: ServiceCall[NotUsed, ServiceInfo] = ServiceCall { _ =>
    import scala.concurrent.ExecutionContext.Implicits.global
    Future(buildInfo)
  }
  override def healthInfo(simple: Boolean): ServiceCall[NotUsed, HealthInfo] = ServiceCall { _ =>
    implicit val ex: ExecutionContext = scala.concurrent.ExecutionContext.global
    if (simple) aggregateHealthInfo(aggregator, ListMap.empty)
    else aggregateHealthInfo(aggregator, healthIndicators)
  }
}

object LagomServiceInfoAutoService {
  def aggregateHealthInfo(aggregator: HealthAggregator, healthIndicators: ListMap[String, HealthIndicator])(implicit ex: ExecutionContext): Future[HealthInfo] = {
    val serviceHealth = Future((applicationName, new Health.Builder().up().build()))
    val dependentServicesHealths = healthIndicators.map {
      case (name, indicator) => (name, Future((name, indicator.health())))
    }.values
    Future
      .sequence(serviceHealth +: dependentServicesHealths.toSeq)
      .map(_.toMap)
      .map(_.asJava)
      .map(aggregator.aggregate)
  }
}
