package com.goodla.datastore

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.goodla.datastore.json.{CacheKey, CacheKeyValue}
import com.goodla.datastore.json.CacheRequestJsonSupport._
import com.hazelcast.config._
import com.hazelcast.Scala._
import com.hazelcast.core.HazelcastInstance
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait GoodlaDataStoreService extends LazyLogging {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer


  val conf: Config = new Config("goodla-ds")
  serialization.Defaults.register(conf.getSerializationConfig)
  val hz: HazelcastInstance = conf.newInstance()

  val putCacheRoute: Route =
    path("cache") {
      post {
        entity(as[CacheKeyValue]) { cacheRequest =>

          val result = Try(hz.getMap(cacheRequest.tableName).put(cacheRequest.cacheKey, cacheRequest.cacheValue)) match {
            case Success(_) => s"Added: $cacheRequest"
            case Failure(exception) => s"Error while adding ${exception.getMessage}"
          }

          logger.info(result)

          complete(result)

        }
      }
    }

  val getCacheRoute: Route =
    path("cache") {
      get {
        parameters('tableName.as[String], 'key.as[String]).as(CacheKey) { cacheKey =>

          val result = Try(hz.getMap(cacheKey.tableName).get(cacheKey.cacheKey).toString) match {
            case Success(cacheValue) => s"Value found: $cacheValue"
            case Failure(_) => s"Value not found for $cacheKey"
          }

          logger.info(result)

          complete(result)

        }
      }
    }

  val flushCacheRoute: Route =
    path("cache") {
      delete {
        parameters('tableName.as[String]) { cacheTable =>

          val result = Try(hz.getMap(cacheTable).clear()) match {
            case Success(_) => s"Flushed cache: $cacheTable"
            case Failure(exception) => s"Failed to flush $cacheTable. Reason: ${exception.getMessage}"
          }

          logger.info(result)

          complete(result)

        }
      }
    }

}

class GoodlaDataStoreServer(implicit val system:ActorSystem,
                            implicit  val materializer:ActorMaterializer) extends GoodlaDataStoreService {

  def startServer(address: String, port: Int): Future[Http.ServerBinding] = {
    Http().bindAndHandle(putCacheRoute ~ getCacheRoute ~ flushCacheRoute, address, port)
  }

}

object DataStoreServer {

  def main(args: Array[String]) {

    val port: Int = sys.env.getOrElse("PORT", "8080").toInt

    implicit val actorSystem: ActorSystem = ActorSystem("goodla-ds-server")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val server = new GoodlaDataStoreServer
    server.startServer("0.0.0.0", port)

  }


}
