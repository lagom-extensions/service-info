package com.lightbend.lagom.serviceinfo.actuate

import org.springframework.boot.actuate.health.{Health, HealthIndicator}

/**
  * Aerospike health check
  */
object AerospikeDBHealthIndicatorProvider extends HealthIndicatorProvider[IsAerospikeHealthCheckClient] {
  override def provide(aerospikeClient: IsAerospikeHealthCheckClient): HealthIndicator = { () =>
    if (aerospikeClient.isConnected) new Health.Builder().up().build()
    else new Health.Builder().down().build()
  }
}

trait IsAerospikeHealthCheckClient {
  def isConnected: Boolean
}
