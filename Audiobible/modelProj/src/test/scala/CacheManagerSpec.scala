import com.agapep.model.{BookReference, CacheManager}
import java.util.concurrent.{TimeUnit, CountDownLatch}
import java.util.{Observable, Observer}
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import org.scalatest.time.{Milliseconds, Millis}
import scala.collection.immutable.IndexedSeq

/**
 * Created by slovic on 05.03.14.
 */


class CacheManagerSpec extends FlatSpec with MockFactory
with Matchers{

  // Default implementation in trait Suite
//  override def withFixture(test: NoArgTest) = { test() }

  "CacheManager" should "be Obserwable" in {
    val latch:CountDownLatch = new CountDownLatch(1)
    val ref = BookReference("9788370144197/0/0/1")
    val iSeeYou: Observer = new Observer {
      override def update(obs: Observable, arg: scala.Any): Unit = {
        obs.asInstanceOf[CacheManager].getState(arg.asInstanceOf[BookReference])
        latch.synchronized { latch.countDown() }
      }
    }

    val cache = mock[CacheManager]

    (cache.addObserver _).expects(iSeeYou, true)
    (cache.getState _).expects(ref).returns(0)
    (cache.setState _).expects(ref, 0)


    cache.addObserver(iSeeYou, true)
    cache.setState(ref, 0)
    //cache.getProgress(ref)
    latch.synchronized {
      latch.await(1000L,TimeUnit.MILLISECONDS)
      cache.getState(ref) //TODO źle używam!!!
    }
    //cache.getProgress(ref)

  }

  trait MockHack {
    def withFixture(test: CacheManagerSpec.this.NoArgTest): Unit = test()
  }
}
