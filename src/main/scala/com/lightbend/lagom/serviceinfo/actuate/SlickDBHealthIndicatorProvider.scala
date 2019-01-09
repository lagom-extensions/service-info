package com.lightbend.lagom.serviceinfo.actuate

import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator
import slick.jdbc.DataSourceJdbcDataSource
import slick.jdbc.JdbcBackend.Database

/**
  * Slick JDBC health check
  */
object SlickDBHealthIndicatorProvider extends HealthIndicatorProvider[Database] {
  override def provide(db: Database): HealthIndicator =
    new DataSourceHealthIndicator(db.source.asInstanceOf[DataSourceJdbcDataSource].ds)
}
