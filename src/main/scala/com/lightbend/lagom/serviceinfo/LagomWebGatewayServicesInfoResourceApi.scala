package com.lightbend.lagom.serviceinfo

import com.lightbend.lagom.buildinfo.LagomBuildInfo
import com.lightbend.lagom.serviceinfo.HealthInfo.simpleParam
import com.lightbend.lagom.serviceinfo.LagomServiceInfoAutoService.aggregateHealthInfo
import org.springframework.boot.actuate.health.{HealthIndicator, OrderedHealthAggregator}
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

class LagomWebGatewayServicesInfoResourceApi(
    controllerComponents: ControllerComponents,
    webGateWayBuildInfo: LagomBuildInfo,
    serviceInfoServices: Seq[LagomServiceInfoService],
    healthIndicators: ListMap[String, HealthIndicator] = ListMap.empty[String, HealthIndicator]
)(implicit exec: ExecutionContext)
    extends AbstractController(controllerComponents)
    with LagomServiceAggreage {

  private val aggregator = new OrderedHealthAggregator()

  def getServiceInfo: Action[AnyContent] = Action.async {
    Future(Ok(Json.toJson(ServiceInfo.buildInfoToServiceInfo(webGateWayBuildInfo))))
  }

  def getHealthInfo: Action[AnyContent] = Action.async { implicit request =>
    webGatewayHeathInfo.map(healthInfo => Ok(Json.toJson(healthInfo)))
  }

  def getServicesInfo: Action[AnyContent] = Action.async {
    val webGatewayInfo = Future.successful(ServiceInfo.buildInfoToServiceInfo(webGateWayBuildInfo))
    val microServicesInfos = serviceInfoServices.map(
      s => s.getServiceInfo.invoke().recover { case _ => ServiceInfo(s.descriptor.name, "", Map()) }
    )
    Future.sequence(webGatewayInfo +: microServicesInfos).map(infos => Ok(Json.toJson(infos)))
  }

  def getHealthesInfo: Action[AnyContent] = Action.async { implicit request =>
    val servicesInfos = servicesHealthInfo(serviceInfoServices, isSimple)
    val webGatewayHealth = webGatewayHeathInfo.map(toServiceHealthInfo(webGateWayBuildInfo.service, _))
    Future
      .sequence(webGatewayHealth +: servicesInfos)
      .map(infos => Ok(Json.toJson(infos)))
  }

  private def webGatewayHeathInfo(implicit request: Request[AnyContent]): Future[HealthInfo] = {
    if (isSimple) aggregateHealthInfo(aggregator, ListMap.empty)
    else aggregateHealthInfo(aggregator, healthIndicators)
  }

  private def isSimple(implicit request: Request[AnyContent]): Boolean = {
    request.queryString.get(simpleParam).map(_.head) match {
      case Some(param) if param == "false" => false
      case _                               => true
    }
  }
}
