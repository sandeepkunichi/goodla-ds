package com.goodla.datastore.data

case class CacheKeyValue(tableName: String, cacheKey: String, cacheValue: String)
case class CacheKey(tableName: String, cacheKey: String)