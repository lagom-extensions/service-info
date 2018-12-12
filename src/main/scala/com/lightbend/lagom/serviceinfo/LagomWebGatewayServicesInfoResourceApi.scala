package com.lightbend.lagom.serviceinfo

import com.lightbend.lagom.buildinfo.LagomBuildInfo
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class LagomWebGatewayServicesInfoResourceApi(
    controllerComponents: ControllerComponents,
    webGateWayBuildInfo: LagomBuildInfo,
    versionQueryServices: Seq[LagomServiceInfoService]
)(implicit exec: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def getServiceInfo: Action[AnyContent] = Action.async {
    Future(Ok(Json.toJson(ServiceInfo.buildInfoToServiceInfo(webGateWayBuildInfo))))
  }

  def getServicesInfo: Action[AnyContent] = Action.async {
    val webGatewayInfo = Future.successful(ServiceInfo.buildInfoToServiceInfo(webGateWayBuildInfo))
    val microServicesInfos = versionQueryServices.map(
      s => s.getServiceInfo.invoke().recover { case _ => ServiceInfo(s.descriptor.name, "", Map()) }
    )
    Future.sequence(webGatewayInfo +: microServicesInfos).map(infos => Ok(Json.toJson(infos)))
  }
}
