import com.agapep.model.BookReference
import org.scalatest.{Matchers, FlatSpec}
import scala.util.{Success, Try, Failure}

/**
 * Created by slovic on 05.03.14.
 */
class BookReferenceSpec extends FlatSpec with Matchers {

  def withGuard[T] (ref: => T,
                 onSucc: => (T) => Any = (c:T) => c,
                 onFail: => (Throwable) => Any = fail("can't build item",_)) {
    Try(ref) match {
      case Failure(x) => onFail(x)
      case Success(x) => { onSucc(x) }
    }
  }

  def testRefType(values: (Boolean, Boolean, Boolean, Int, Int, Int, Int, Int))(ref: BookReference) {
    ref.isChapterReference should be (values._1)
    ref.isVersionReference should be (values._2)
    ref.isAudioReference should be (values._3)
    ref.chapter should be (values._4)
    ref.chapterVersion should be (values._5)
    ref.fileId should be (values._6)
    ref.time should be (values._7)
    ref.timeEnd should be (values._8)
  }

  "BookReference" should "be able to be constructed from string" in {
    withGuard( BookReference("9788370144197/0"),
      testRefType((true, false, false, 0, -1, -1, -1, -1)))

    withGuard( BookReference("9788370144197/0/0"),
      testRefType((true, true, false, 0, 0, -1, -1, -1)))

    withGuard( BookReference("9788370144197/0/0/0"),
      testRefType((true, true, true, 0, 0, 0, -1, -1)))

    withGuard( BookReference("9788370144197/0/0/0/1"),
      testRefType((true, true, true, 0, 0, 0, 1, -1)))

    withGuard( BookReference("9788370144197/0/0/0/1/2"),
      testRefType((true, true, true, 0, 0, 0, 1, 2)))
  }

  it should "fail if construction string is wrong (letters, to many args, double slash inside)" in {
    def failGuard[T](constStr: String) = withGuard(
      BookReference(constStr), fail("constructed from string: "+ constStr),_ => ())
    failGuard("9788370144197a/0")
    failGuard("9788370144197/0/1//2/")
    failGuard("9788370144197/0a")
    failGuard("9788370144197/0/a")
    failGuard("9788370144197/0/4/4/4/4/4/4")
  }

  it should "have toString result identically to construct string" in {
    def testStrToBookToStr(str: String) {
      withClue("problem z :"+str+ " !") { BookReference(str).toString should equal (str) }
    }
    testStrToBookToStr( "9788370144197/0")
    testStrToBookToStr( "9788370144197/0/0")
    testStrToBookToStr( "9788370144197/0/0/0")
    testStrToBookToStr( "9788370144197/0/0/0/1")
    testStrToBookToStr( "9788370144197/0/0/0/1/2")
  }

  it should "ignore slash at end of string" in {
    def testStrWithSlashToBookToStr(str: String) {
      withClue("problem z :"+str + "/ !") { BookReference(str+"/").toString should equal (str) }
    }
    testStrWithSlashToBookToStr( "9788370144197/0")
    testStrWithSlashToBookToStr( "9788370144197/0/0")
    testStrWithSlashToBookToStr( "9788370144197/0/0/0")
    testStrWithSlashToBookToStr( "9788370144197/0/0/0/1" )
    testStrWithSlashToBookToStr( "9788370144197/0/0/0/1/2")
  }

  it should "be able to be transforme into a chapter/file reference" in {
    val chapterRef = BookReference("9788370144197/0")
    BookReference("9788370144197/0").toChapterReference should equal(chapterRef)
    BookReference("9788370144197/0/0").toChapterReference should equal(chapterRef)
    BookReference("9788370144197/0/0/0").toChapterReference should equal(chapterRef)
    BookReference("9788370144197/0/0/0/1").toChapterReference should equal(chapterRef)
    BookReference("9788370144197/0/0/0/1/2").toChapterReference should equal(chapterRef)
    val fileRef = BookReference("9788370144197/0/0/0")
    BookReference("9788370144197/0/0/0").toFileReference should equal(fileRef)
    BookReference("9788370144197/0/0/0/1").toFileReference should equal(fileRef)
    BookReference("9788370144197/0/0/0/1/2").toFileReference should equal(fileRef)
  }

  it should "throw an exception when calls toFileReference being a chapter or version reference" in {
    an[UnsupportedOperationException] should be thrownBy BookReference("9788370144197/0").toFileReference
    an[UnsupportedOperationException] should be thrownBy BookReference("9788370144197/0/0").toFileReference
  }

  it should "be equal to another object created from the same string" in {
    val str = "9788370144197/0/0/0/1"
    BookReference(str).equals(BookReference(str)) should be (true)
    BookReference(str).equals(BookReference(str+"/2")) should be (false)
  }

  it should "provide fileEquals method" in {
    val bar1 = BookReference("9788370144197/0/0/0/1/2")
    val bar2 = BookReference("9788370144197/0/0/0/12/14")
    val bar3 = BookReference("9788370144197/0/0/1/11/155")
    val bar4 = BookReference("9788370144197/0/1/0/11/155")
    val bar5 = BookReference("9788370144197/1/0/0/11/155")
    val bar6 = BookReference("9788370144197/1/0/0")
    bar1.fileEquals(bar2) should be(true)
    bar2.fileEquals(bar1) should be(true)
    bar1.fileEquals(bar3) should be(false)
    bar1.fileEquals(bar4) should be(false)
    bar1.fileEquals(bar5) should be(false)
    bar5.fileEquals(bar6) should be(true)
  }

  it should "throw en exception when calls fileEquals method being chapter or version reference" in {
    val bar1 = BookReference("9788370144197/1/0/0")
    val bar2 = BookReference("9788370144197/1/0")
    an[UnsupportedOperationException] should be thrownBy(bar1.fileEquals(bar2))
  }
}
