package com.agapep.model


import scala.collection.mutable.Map
import scala.collection.mutable
import scala.collection.script._
import javax.swing.text.DefaultEditorKit.InsertBreakAction

/**
 * Created by slovic on 06.03.14.
 */
class CacheStore[A ,B](defo: B) extends mutable.HashMap[A, B] with mutable.ObservableMap[A,B] {
  type Pub = CacheStore[A,B]
  override def default(key:A) = defo //TODO default get data from db

  override def update(key: A, value: B): Unit = {
    val oldValue = apply(key)
    if (value == oldValue) println ("_updated:" + (key, value))
    else println ("not_updated:" +  (key, value))
    super.update(key, value)
  }

  subscribe(sub)
  lazy val sub = new Sub {
    override def notify(pub: Pub, event: Message[(A, B)] with mutable.Undoable): Unit = event match {
      case _:Include[_] => println(event)
      case _:Remove[_] => println(event)
      case _:Reset[_] => println(event)
      case _:Update[_] => println(event)
      //TODO update databese
    }
  }
}
//
//class DB {
//  lazy val db = Database.forURL("jdbc:sqlite:" +
//    getApplicationContext().getFilesDir() +
//    "slick-sandbox.txt", driver = "org.sqldroid.SQLDroidDriver")
//}

trait Cached {
  def toCached = toString()
  def pull[B](implicit store: CacheStore[String,B]):B = store.apply(toCached)
  def push[B](value:B)(implicit store: CacheStore[String,B]):Unit = store += ((toCached, value))
}
