package com.agapep.model

import scala.io.Source
import scala.collection.script.Message
import scala.collection.mutable

/**
 * Created by slovic on 06.03.14.
 */
object Main extends App {
  def path = Source.fromURL(getClass.getResource("/www_biblia_mp3_pl.csv"))
  override def main(args: Array[String]): Unit = {
    import MeasureTime._
    super.main(args)
    val file = path
    //    val book = Book(BookInfo(BigInt("9788370144197") ,"Pismo Święte") , file.getLines().toList)
    //println (book.audioSourcesV(book.versions(0)))

    val bar = BookReference("9788370144197/0/0/0/1///");
    printf (bar + "\n")
    //    printf (Try(BookReference2("9788370144197/0a")).toString)
    //    for {
    //      chapter <- book.chapters.find(_.abbr == "Rdz")
    //      source <- book.audioSources.filter(_.chapter == chapter)
    //    } println(  book.cacheFile(source) )

    val cache = new CacheStore[String, Int](0)
    val str = "wait for me :)"

    val sub = new cache.Sub{
      override def notify(pub: cache.Pub, event: Message[(String, Int)] with mutable.Undoable): Unit = println(event)
    }

    cache.subscribe(sub)
    cache.activateSubscription(sub)
    cache += ((str,11))
    cache += ((str,11))
    cache -= (str)
    cache.clear()
    cache.++=( (str+"/", 14)::(str, 14)::(str, 13)::Nil )
    //cache.update(str, 12)
  }
}
