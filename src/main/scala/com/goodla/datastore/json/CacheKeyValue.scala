package com.goodla.datastore.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class CacheKeyValue(tableName: String, cacheKey: String, cacheValue: String)
case class CacheKey(tableName: String, cacheKey: String)

object CacheRequestJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val cacheRequestFormats: RootJsonFormat[CacheKeyValue] = jsonFormat3(CacheKeyValue)
}
