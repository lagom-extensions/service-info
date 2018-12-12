package com.lightbend.lagom.buildinfo

trait LagomBuildInfo {
  val service: String
  val version: String
  def toMap: Map[String, Any]
}
