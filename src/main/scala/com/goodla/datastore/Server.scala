package com.goodla.datastore

import com.hazelcast.config._
import com.hazelcast.Scala._


object Server extends App {
  val conf: Config = new Config("goodla-ds")
  serialization.Defaults.register(conf.getSerializationConfig)
  val hz = conf.newInstance()
  hz.getMap("goodla-ds").put("test_key", "test_value")
  println(hz.getMap("goodla-ds").get("test_key"))
}
