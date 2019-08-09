package com.goodla.datastore.xdc

import pureconfig.{CamelCase, ConfigFieldMapping}
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._

case class CacheNode(name: String, uri: String, me: Boolean)

class XDCConfig {

  def getOtherNodes: Seq[CacheNode] = {
    implicit def productHint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
    pureconfig.loadConfigOrThrow[Seq[CacheNode]]("nodes").filter(n => !n.me)
  }

}
