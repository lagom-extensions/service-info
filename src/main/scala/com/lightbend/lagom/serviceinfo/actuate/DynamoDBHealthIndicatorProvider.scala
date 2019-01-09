package com.lightbend.lagom.serviceinfo.actuate

import akka.Done
import akka.stream.alpakka.dynamodb.scaladsl.DynamoClient
import akka.stream.alpakka.dynamodb.scaladsl.DynamoImplicits._
import cats.effect.{Async, IO}
import com.amazonaws.services.dynamodbv2.model._
import org.springframework.boot.actuate.health.{Health, HealthIndicator}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * DynamoDB health check
  * approach is like 'select 1 from dual' but on preconfigured table
  * ensure table exists, or app create table permission
  * there is no AWS TableExistRequest, to work on any table, so this simple check
  */
object DynamoDBHealthIndicatorProvider extends HealthIndicatorProvider[DynamoDBHealthCheckContext] {
  override def provide(context: DynamoDBHealthCheckContext): HealthIndicator = { () =>
    {
      implicit val ex: ExecutionContext = context.executionContext
      val successCheckF = for {
        _ <- isCheckSuccess(context).recover { case _: ResourceNotFoundException => createTable(context) }
      } yield true

      val ioDynamoCheck: IO[Boolean] =
        Async[IO].async { cb =>
          successCheckF.onComplete {
            case Success(value) => cb(Right(value))
            case Failure(_)     => cb(Right(false))
          }
        }

      if (ioDynamoCheck.unsafeRunSync()) new Health.Builder().up().build()
      else new Health.Builder().down().build()
    }
  }

  private def isCheckSuccess(context: DynamoDBHealthCheckContext): Future[Done] =
    context.dynamoClient
      .single(
        new GetItemRequest()
          .withTableName(context.checkTableName)
          .withAttributesToGet("x")
          .withKey(Map("x" -> new AttributeValue("-1")).asJava)
      )
      .map(_ => Done)(context.executionContext)

  private def createTable(context: DynamoDBHealthCheckContext): Future[Done] =
    context.dynamoClient
      .single(
        new CreateTableRequest()
          .withTableName(context.checkTableName)
          .withKeySchema(new KeySchemaElement("x", KeyType.HASH))
          .withAttributeDefinitions(new AttributeDefinition("x", ScalarAttributeType.S))
          .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(context.readCapacityUnits))
      )
      .map(_ => Done)(context.executionContext)
      .recover { case _: ResourceInUseException => Done }(context.executionContext)

}

case class DynamoDBHealthCheckContext(dynamoClient: DynamoClient, executionContext: ExecutionContext, checkTableName: String = "apps_health_indicator_dual_table", readCapacityUnits: Long = 1)
