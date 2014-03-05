import com.agapep.model.BookReference2
import org.scalatest.{Matchers, FlatSpec}
import scala.util.{Success, Try, Failure}

/**
 * Created by slovic on 05.03.14.
 */
class BookReferenceTest extends FlatSpec with Matchers {

  def withGuard[T] (ref: => T,
                 onSucc: => (T) => Any = (c:T) => c,
                 onFail: => (Throwable) => Any = fail("can't build item",_)) {
    Try(ref) match {
      case Failure(x) => onFail(x)
      case Success(x) => { onSucc(x) }
    }
  }

  def testRefType(values: (Boolean, Boolean, Boolean, Int, Int, Int, Int, Int))(ref: BookReference2) {
    ref.isChapterReference should be (values._1)
    ref.isVersionReference should be (values._2)
    ref.isAudioReference should be (values._3)
    ref.chapter should be (values._4)
    ref.chapterVersion should be (values._5)
    ref.fileId should be (values._6)
    ref.time should be (values._7)
    ref.timeEnd should be (values._8)
  }

  "BookReference" should "can be constructed from string" in {
    withGuard( BookReference2("9788370144197/0"),
      testRefType((true, false, false, 0, -1, -1, -1, -1)))

    withGuard( BookReference2("9788370144197/0/0"),
      testRefType((true, true, false, 0, 0, -1, -1, -1)))

    withGuard( BookReference2("9788370144197/0/0/0"),
      testRefType((true, true, true, 0, 0, 0, -1, -1)))

    withGuard( BookReference2("9788370144197/0/0/0/1"),
      testRefType((true, true, true, 0, 0, 0, 1, -1)))

    withGuard( BookReference2("9788370144197/0/0/0/1/2"),
      testRefType((true, true, true, 0, 0, 0, 1, 2)))
  }

  it should "fail if construction string is wrong" in {
    def failGuard[T](constStr: String) = withGuard(
      BookReference2(constStr), fail("constructed from string: "+ constStr),_ => ())
    failGuard("9788370144197a/0")
    failGuard("9788370144197/0a")
    failGuard("9788370144197/0/a")
    failGuard("9788370144197/0/4/4/4/4/4/4")
  }

  it should "has toString result identically to construct string" in {
    def testStrToBookToStr(str: String) {
      withClue("problem z :"+str+ " !") {
        BookReference2(str).toString should equal (str)
      }
    }
    testStrToBookToStr( "9788370144197/0")
    testStrToBookToStr( "9788370144197/0/0")
    testStrToBookToStr( "9788370144197/0/0/0")
    testStrToBookToStr( "9788370144197/0/0/0/1")
    testStrToBookToStr( "9788370144197/0/0/0/1/2")
  }
}
