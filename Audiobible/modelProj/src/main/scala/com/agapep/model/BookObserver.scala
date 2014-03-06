package com.agapep.model

import scala.collection.mutable

/**
 * Created by slovic on 06.03.14.
 */
class BookObserver extends mutable.Subscriber[String, AudioSource] {
  override def notify(pub: AudioSource, event: String): Unit = ???
}
