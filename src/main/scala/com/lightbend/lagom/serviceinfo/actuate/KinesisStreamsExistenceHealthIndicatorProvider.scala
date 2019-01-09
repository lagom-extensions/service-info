package com.lightbend.lagom.serviceinfo.actuate

import java.util
import java.util.concurrent.CompletableFuture

import cats.data.NonEmptyList
import cats.effect.{Async, IO}
import com.amazonaws.services.kinesis.AmazonKinesisAsync
import com.amazonaws.services.kinesis.model.DescribeStreamSummaryRequest
import org.springframework.boot.actuate.health.{Health, HealthIndicator}

import scala.compat.java8.FutureConverters
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Kinesis data streams existence health check
  */
object KinesisStreamsExistenceHealthIndicatorProvider extends HealthIndicatorProvider[KinesisStreamsHealthCheckContext] {
  private val validStreamStatuses = Set("ACTIVE", "UPDATING")
  override def provide(context: KinesisStreamsHealthCheckContext): HealthIndicator = { () =>
    implicit val ex: ExecutionContext = context.executionContext
    val streamsChecks = (for (streamName <- context.streams)
      yield context.kinesis.describeStreamSummaryAsync(new DescribeStreamSummaryRequest().withStreamName(streamName)))
      .map(asScalaFuture)
      .toList
    val successCheckF = Future.sequence(streamsChecks)

    val ioCheck: IO[Boolean] =
      Async[IO].async { cb =>
        successCheckF.onComplete {
          case Success(results) =>
            cb(Right(results.forall { result =>
              result != null &&
              result.getStreamDescriptionSummary != null &&
              validStreamStatuses.contains(result.getStreamDescriptionSummary.getStreamStatus)
            }))
          case Failure(_) => cb(Right(false))
        }
      }

    if (ioCheck.unsafeRunSync()) new Health.Builder().up().build()
    else new Health.Builder().down().build()
  }

  private def asScalaFuture[T](javaFuture: util.concurrent.Future[T]): Future[T] =
    FutureConverters.toScala(CompletableFuture.supplyAsync(() => javaFuture.get))
}

case class KinesisStreamsHealthCheckContext(kinesis: AmazonKinesisAsync, executionContext: ExecutionContext, streams: NonEmptyList[String])
