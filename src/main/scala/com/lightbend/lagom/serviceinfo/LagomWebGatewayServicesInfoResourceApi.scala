package com.lightbend.lagom.serviceinfo

import com.lightbend.lagom.buildinfo.LagomBuildInfo
import com.lightbend.lagom.serviceinfo.LagomServiceInfoAutoService.aggregateHealthInfo
import org.springframework.boot.actuate.health.{HealthIndicator, OrderedHealthAggregator}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

class LagomWebGatewayServicesInfoResourceApi(
    controllerComponents: ControllerComponents,
    webGateWayBuildInfo: LagomBuildInfo,
    serviceInfoServices: Seq[LagomServiceInfoService],
    healthIndicators: ListMap[String, HealthIndicator] = ListMap.empty[String, HealthIndicator]
)(implicit exec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  private val aggregator = new OrderedHealthAggregator()
  private def webGatewayHealthInfo: Future[HealthInfo] = aggregateHealthInfo(aggregator, healthIndicators)

  def getServiceInfo: Action[AnyContent] = Action.async {
    Future(Ok(Json.toJson(ServiceInfo.buildInfoToServiceInfo(webGateWayBuildInfo))))
  }

  def getHealthInfo: Action[AnyContent] = Action.async {
    webGatewayHealthInfo.map(healthInfo => Ok(Json.toJson(healthInfo)))
  }

  def getServicesInfo: Action[AnyContent] = Action.async {
    val webGatewayInfo = Future.successful(ServiceInfo.buildInfoToServiceInfo(webGateWayBuildInfo))
    val microServicesInfos = serviceInfoServices.map(
      s => s.getServiceInfo.invoke().recover { case _ => ServiceInfo(s.descriptor.name, "", Map()) }
    )
    Future.sequence(webGatewayInfo +: microServicesInfos).map(infos => Ok(Json.toJson(infos)))
  }

  def getHealthesInfo: Action[AnyContent] = Action.async {
    def toServiceHealthInfo(service: String, healthInfo: HealthInfo): ServiceHealthInfo = {
      ServiceHealthInfo(service, healthInfo.statusCode, healthInfo.details)
    }
    val servicesInfos = serviceInfoServices.map(
      s =>
        s.healthInfo
          .invoke()
          .map(healthInfo => toServiceHealthInfo(s.descriptor.name, healthInfo))
          .recover { case _ => ServiceHealthInfo(s.descriptor.name, "DOWN") }
    )
    Future
      .sequence(webGatewayHealthInfo.map(healthInfo => toServiceHealthInfo(webGateWayBuildInfo.service, healthInfo)) +: servicesInfos)
      .map(infos => Ok(Json.toJson(infos)))
  }
}
