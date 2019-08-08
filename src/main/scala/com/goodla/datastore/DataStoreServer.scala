package com.goodla.datastore

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.goodla.datastore.json.CacheRequest
import com.goodla.datastore.json.CacheRequestJsonSupport._
import com.hazelcast.config._
import com.hazelcast.Scala._

import scala.concurrent.ExecutionContext.Implicits.global

object DataStoreServer {

  def main(args: Array[String]) {

    implicit val actorSystem: ActorSystem = ActorSystem("system")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

    val conf: Config = new Config("goodla-ds")
    serialization.Defaults.register(conf.getSerializationConfig)
    val hz = conf.newInstance()

    val route =
      pathSingleSlash {
        post {
          entity(as[CacheRequest]) { cacheRequest =>

            hz.getMap(cacheRequest.tableName).put(cacheRequest.cacheKey, cacheRequest.cacheValue)

            val storedValue = hz.getMap(cacheRequest.tableName).get(cacheRequest.cacheKey).asInstanceOf[String]
            complete(storedValue)

          }
        }
      }

    Http().bindAndHandle(route, "goodla-ds.herokuapp.com", 80)

  }

}
