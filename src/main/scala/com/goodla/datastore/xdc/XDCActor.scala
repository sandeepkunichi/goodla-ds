package com.goodla.datastore.xdc

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.SourceQueueWithComplete
import com.goodla.datastore.data.CacheKeyValue
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

case class CacheKeyValueMessage(cacheKeyValue: CacheKeyValue)
case class CacheKeyValuesMessage(cacheKeyValues: Seq[CacheKeyValue])

class XDCActor extends Actor with LazyLogging with SprayJsonSupport {

  implicit val system: ActorSystem = ActorSystem("goodla-ds-server")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val queue: SourceQueueWithComplete[CacheQueueElement] = GoodlaDsQueue.getQueue

  override def receive: PartialFunction[Any, Unit] = {
    case CacheKeyValueMessage(cacheKeyValue) => queue offer CacheQueueElement(cacheKeyValue)
    case CacheKeyValuesMessage(cacheKeyValues) => cacheKeyValues.map { cacheKeyValue =>
      queue offer CacheQueueElement(cacheKeyValue)
    }
    case _ => logger.error(s"Received unknown message")
  }



}
