package com.goodla.datastore.xdc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, RequestEntity}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.goodla.datastore.data.CacheKeyValue
import com.goodla.datastore.data.json.CacheDataJsonSupport._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


case class CacheQueueElement(cacheKeyValue: CacheKeyValue)
case class CacheSyncResponse(cacheNode: CacheNode, httpResponse: Future[HttpResponse])

object GoodlaDsQueue extends LazyLogging {

  implicit val actorSystem: ActorSystem = ActorSystem("goodla-ds-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  val http = Http(actorSystem)

  val xdcConfig = new XDCConfig
  val otherNodes: Seq[CacheNode] = xdcConfig.getOtherNodes

  private val queue = Source.queue[CacheQueueElement](bufferSize = 100, OverflowStrategy.backpressure)
    .mapAsyncUnordered(Int.MaxValue) { elem =>
      Future {
        postCacheKeyValue(elem.cacheKeyValue)
      }(scala.concurrent.ExecutionContext.global)
    }.to(Sink.ignore)
    .run


  def getQueue: SourceQueueWithComplete[CacheQueueElement] = queue

  def postCacheKeyValue(cacheKeyValue: CacheKeyValue): Unit = {
    for {
      futureResponse <- otherNodes.map { node =>
        val response = Marshal(cacheKeyValue).to[RequestEntity] flatMap { entity =>
          val request = HttpRequest(method = HttpMethods.POST, uri = s"${node.uri}/cache", entity = entity)
          http.singleRequest(request)
        }
        CacheSyncResponse(node, response)
      }
    } yield {
      futureResponse.httpResponse onComplete {
        case Failure(ex) => logger.error(s"Failed to post $cacheKeyValue, reason: $ex")
        case Success(response) => logger.info(s"${futureResponse.cacheNode.name} responded with $response")
      }
    }
  }

}
