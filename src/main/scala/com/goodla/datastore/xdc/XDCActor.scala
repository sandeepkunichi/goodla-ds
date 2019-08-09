package com.goodla.datastore.xdc

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity}
import akka.stream.ActorMaterializer
import com.goodla.datastore.data.CacheKeyValue
import com.goodla.datastore.data.json.CacheDataJsonSupport._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

case class CacheKeyValueMessage(cacheKeyValue: CacheKeyValue)
case class CacheKeyValuesMessage(cacheKeyValues: Seq[CacheKeyValue])

class XDCActor extends Actor with LazyLogging with SprayJsonSupport {

  implicit val system: ActorSystem = ActorSystem("goodla-ds-server")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val http = Http(system)

  val xdcConfig = new XDCConfig
  val otherNodes: Seq[CacheNode] = xdcConfig.getOtherNodes

  override def receive: PartialFunction[Any, Unit] = {
    case CacheKeyValueMessage(cacheKeyValue) => postCacheKeyValue(cacheKeyValue)
    case CacheKeyValuesMessage(cacheKeyValues) => cacheKeyValues.foreach(postCacheKeyValue)
    case _ => logger.error(s"Received unknown message")
  }

  def postCacheKeyValue(cacheKeyValue: CacheKeyValue): Unit = {
    for {
      futureResponse <- otherNodes.map { node =>
        Marshal(cacheKeyValue).to[RequestEntity] flatMap { entity =>
          val request = HttpRequest(method = HttpMethods.POST, uri = s"${node.uri}/cache", entity = entity)
          http.singleRequest(request)
        }
      }
    } yield {
      futureResponse onComplete {
        case Failure(ex) => logger.error(s"Failed to post $cacheKeyValue, reason: $ex")
        case Success(response) => logger.info(s"Server responded with $response")
      }
    }
  }


}
