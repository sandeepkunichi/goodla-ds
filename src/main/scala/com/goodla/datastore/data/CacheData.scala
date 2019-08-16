package com.goodla.datastore.data

case class CacheKeyValue(tableName: String, cacheKey: String, cacheValue: String, external: Boolean){
  def copyAsInternal: CacheKeyValue = {
    CacheKeyValue(tableName, cacheKey, cacheValue, external = false)
  }
}
case class CacheKey(tableName: String, cacheKey: String)