package com.lightbend.lagom.serviceinfo.actuate

import cats.effect.{Async, IO}
import com.lightbend.lagom.serviceinfo.{LagomServiceAggreage, LagomServiceInfoService, ServiceHealthInfo}
import org.springframework.boot.actuate.health.{Health, HealthIndicator, OrderedHealthAggregator}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Lagom service simple health check indicator
  */
object LagomServiceIndicatorProvider extends HealthIndicatorProvider[ServicesHealthChecksContext] with LagomServiceAggreage {
  private val simpleCheck = true
  private val aggregator = new OrderedHealthAggregator()

  override def provide(context: ServicesHealthChecksContext): HealthIndicator = { () =>
    implicit val ec: ExecutionContext = context.ec
    val successCheckF = Future.sequence(servicesHealthInfo(context.services, simpleCheck))
    val ioCheck: IO[Health] =
      Async[IO].async { cb =>
        successCheckF.onComplete {
          case Success(healthSeq) => cb(Right(toSingleHealth(healthSeq)))
          case Failure(_)         => cb(Right(Health.down().build()))
        }
      }
    ioCheck.unsafeRunSync()
  }

  def toSingleHealth(servicesHealth: Seq[ServiceHealthInfo]): Health = {
    val services = (
      for (serviceHealth <- servicesHealth)
        yield (serviceHealth.service, new Health.Builder().status(serviceHealth.statusCode).build())
    ).toMap
    aggregator.aggregate(services.asJava)
  }
}

case class ServicesHealthChecksContext(services: Seq[LagomServiceInfoService], ec: ExecutionContext)
