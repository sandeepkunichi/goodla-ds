package com.goodla.datastore

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.goodla.datastore.json.{CacheKey, CacheKeyValue}
import com.goodla.datastore.json.CacheRequestJsonSupport._
import com.goodla.datastore.xdc.{CacheKeyValueMessage, CacheKeyValuesMessage, XDCActor}
import com.hazelcast.config._
import com.hazelcast.Scala._
import com.hazelcast.core.HazelcastInstance
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait GoodlaDataStoreService extends LazyLogging {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer

  val xdcActor: ActorRef = system.actorOf(
    Props(new XDCActor()),
    name = "xdcActor"
  )



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

          xdcActor ! CacheKeyValueMessage(cacheRequest)

          complete(result)

        }
      }
    }

  val putAllCacheRoute: Route =
    path("cache") {
      post {
        entity(as[Seq[CacheKeyValue]]) { cacheRequests =>

          val result = Try({
            cacheRequests.map { cacheRequest =>
              hz.getMap(cacheRequest.tableName).put(cacheRequest.cacheKey, cacheRequest.cacheValue)
            }
          }) match {
            case Success(_) => s"Added: $cacheRequests"
            case Failure(exception) => s"Error while adding ${exception.getMessage}"
          }

          logger.info(result)

          xdcActor ! CacheKeyValuesMessage(cacheRequests)

          complete(result)

        }
      }
    }

  val getCacheRoute: Route =
    path("cache") {
      get {
        parameters('tableName.as[String], 'key.as[String]).as(CacheKey) { cacheKey =>

          val result = Try(hz.getMap(cacheKey.tableName).get(cacheKey.cacheKey).toString) match {
            case Success(cacheValue) => CacheKeyValue(cacheKey.tableName, cacheKey.cacheKey, cacheValue)
            case Failure(_) => CacheKeyValue("", "", "")
          }

          logger.info(s"Got value: $result")

          complete(result)

        }
      }
    }

  val getAllCacheRoute: Route =
    path("cache") {
      get {
        parameters('tableName.as[String]) { cacheTable =>

          val result: Seq[CacheKeyValue] = Try(hz.getMap(cacheTable)) match {
            case Success(cacheMap) => (for { entry <- cacheMap.entrySet } yield { CacheKeyValue(cacheTable, entry.getKey, entry.getValue)}).toSeq
            case Failure(_) => Seq.empty
          }

          logger.info(s"Getting all values")

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
                            implicit val materializer:ActorMaterializer) extends GoodlaDataStoreService {

  def startServer(address: String, port: Int): Future[Http.ServerBinding] = {
    Http().bindAndHandle(putCacheRoute ~ putAllCacheRoute ~ getCacheRoute ~ getAllCacheRoute ~ flushCacheRoute, address, port)
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
