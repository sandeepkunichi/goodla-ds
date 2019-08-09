package com.goodla.datastore.data.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.goodla.datastore.data.CacheKeyValue
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object CacheDataJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val cacheKeyValueFormats: RootJsonFormat[CacheKeyValue] = jsonFormat3(CacheKeyValue)
}
