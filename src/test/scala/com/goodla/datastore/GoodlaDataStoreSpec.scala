package com.goodla.datastore

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.{Matchers, WordSpec}


class GoodlaDataStoreSpec extends WordSpec with Matchers with ScalatestRouteTest with GoodlaDataStoreService {

  "Goodla DataStore API" should {

    "Posting to /cache should add to cache" in {

      val jsonRequest = ByteString(
        s"""
           |{
           |	"tableName": "goodla-ds-test",
           |	"cacheKey": "test_key",
           |	"cacheValue": "test_value"
           |}
        """.stripMargin)

      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/cache",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest))

      postRequest ~> putCacheRoute ~> check {
        status.isSuccess() shouldEqual true
        entityAs[String] should ===("""Added: CacheKeyValue(goodla-ds-test,test_key,test_value)""")
      }
    }

    "Getting from /cache should get from cache" in {

      val getRequest = HttpRequest(
        HttpMethods.GET,
        uri = "/cache?tableName=goodla-ds-test&key=test_key")

      getRequest ~> getCacheRoute ~> check {
        status.isSuccess() shouldEqual true
        entityAs[String] should ===("""Value found: test_value""")
      }
    }

    "Flushing from /cache should clear the cache" in {

      val getRequest = HttpRequest(
        HttpMethods.DELETE,
        uri = "/cache?tableName=goodla-ds-test")

      getRequest ~> flushCacheRoute ~> check {
        status.isSuccess() shouldEqual true
        entityAs[String] should ===("""Flushed cache: goodla-ds-test""")
      }
    }

  }

}