package com.lightbend.lagom.serviceinfo.actuate

import org.springframework.boot.actuate.health.HealthIndicator

trait HealthIndicatorProvider[T] {
  def provide(context: T) : HealthIndicator
}
