package com.goodla.datastore

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.goodla.datastore.json.CacheRequest
import com.hazelcast.config._
import com.hazelcast.Scala._

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

            complete(hz.getMap(cacheRequest.tableName).get(cacheRequest.cacheKey))

          }
        }
      }

    Http().bindAndHandle(route, "localhost", 443)

  }

}
