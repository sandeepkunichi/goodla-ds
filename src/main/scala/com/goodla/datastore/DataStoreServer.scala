package com.goodla.datastore

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.goodla.datastore.json.{CacheKey, CacheKeyValue}
import com.goodla.datastore.json.CacheRequestJsonSupport._
import com.hazelcast.config._
import com.hazelcast.Scala._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object DataStoreServer extends LazyLogging {

  def main(args: Array[String]) {

    val port: Int = sys.env.getOrElse("PORT", "8080").toInt

    implicit val actorSystem: ActorSystem = ActorSystem("system")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val conf: Config = new Config("goodla-ds")
    serialization.Defaults.register(conf.getSerializationConfig)
    val hz = conf.newInstance()

    val putCacheRoute =
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

    val getCacheRoute =
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


    Http().bindAndHandle(putCacheRoute ~ getCacheRoute, "0.0.0.0", port)

  }


}
