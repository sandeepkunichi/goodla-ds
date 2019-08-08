package com.goodla.datastore.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class CacheRequestKeyValue(key: String, value: String)
case class CacheRequest(tableName: String, cacheKey: String, cacheValue: String)

object CacheRequestJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val cacheRequestKeyValueFormats: RootJsonFormat[CacheRequestKeyValue] = jsonFormat2(CacheRequestKeyValue)
  implicit val cacheRequestFormats: RootJsonFormat[CacheRequest] = jsonFormat3(CacheRequest)
}
