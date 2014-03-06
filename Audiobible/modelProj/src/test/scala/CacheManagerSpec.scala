import com.agapep.model.{BookObserver, CacheStore, BookReference, CacheManager}
import java.util.concurrent.{TimeUnit, CountDownLatch}
import java.util.{Observable, Observer}
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.Timeouts
import org.scalatest.time.{Milliseconds, Millis}
import scala.collection.immutable.IndexedSeq
import scala.collection.mutable
import scala.collection.script.Message
import scala.slick.driver.SQLiteDriver.simple._
import org.scalatest.time.SpanSugar._
import scala.io.Source

/**
 * Created by slovic on 05.03.14.
 */


class CacheManagerSpec extends FlatSpec with MockFactory with Timeouts with Matchers{

  "BookObserver" should "make possible to subscribe CacheStore" in {
    val cache = new CacheStore[String, Int](0)
//
//    class MySub extends mutable.Subscriber[Message[(Int,Int)] with mutable.Undoable, CacheStore[String, Int]] {
//      //def notify(pub: CacheStore[String, Int], evt: Message[(Int,Int)] with mutable.Undoable) { println(evt) }
//      override def notify(pub: CacheStore[String, Int], event: Message[(Int, Int)] with mutable.Undoable): Unit = println(evt)
//    }

//    val observer = new mutable.Subscriber[] {}
//    observer
    val str = "wait for me :)"

    val sub = new cache.Sub{
      override def notify(pub: cache.Pub, event: Message[(String, Int)] with mutable.Undoable): Unit = str.notifyAll() }

    cache.subscribe(sub)
    cache.activateSubscription(sub)
    cache(str) = 11

    failAfter(1400 millis) {
      str.synchronized( str.wait() )
    }

  }

  "Database" should "be able to be created" in {
    lazy val db = Database.forURL("jdbc:sqlite:" +
      Source.fromURL(getClass.getResource("/database.db")).toString() , driver = "org.sqldroid.SQLDroidDriver")
  }

  // Default implementation in trait Suite
//  override def withFixture(test: NoArgTest) = { test() }

//  "CacheManager" should "be Obserwable" in {
//    val latch:CountDownLatch = new CountDownLatch(1)
//    val ref = BookReference("9788370144197/0/0/1")
//    val iSeeYou: Observer = new Observer {
//      override def update(obs: Observable, arg: scala.Any): Unit = {
//        obs.asInstanceOf[CacheManager].getState(arg.asInstanceOf[BookReference])
//        latch.synchronized { latch.countDown() }
//      }
//    }
//
//    val cache = mock[CacheManager]
//
//    (cache.addObserver _).expects(iSeeYou, true)
//    (cache.getState _).expects(ref).returns(0)
//    (cache.setState _).expects(ref, 0)
//
//
//    cache.addObserver(iSeeYou, true)
//    cache.setState(ref, 0)
//    //cache.getProgress(ref)
//    latch.synchronized {
//      latch.await(1000L,TimeUnit.MILLISECONDS)
//      cache.getState(ref) //TODO źle używam!!!
//    }
//    //cache.getProgress(ref)
//
//  }
}
