/**
 * Created by slovic on 11.02.14.
 */
import collection.mutable.Stack
import com.agapep.model._
import org.scalatest._
import scala.io.Source

class BookTestSpec extends FlatSpec with Matchers {
  def path = Source.fromURL(getClass.getResource("/www_biblia_mp3_pl.csv"))
  lazy val book = {
    Book(BookInfo(BigInt("9788370144197") ,"Pismo Święte"), path.getLines().toList)
  }

  "A Book" should "be able to be constructed from file stream without data" in {
    val book = Book(BookInfo(BigInt("9788370144197") ,"Pismo Święte"), Nil)
    book.isbn should be (BigInt("9788370144197"))
    book.name should be ("Pismo Święte")
  }

  it should "be constructed from file stream" in {
    Book.readFromCsvData(BookInfo(BigInt("9788370144197") ,"Pismo Święte"), path.getLines().toList)
  }

  it should "goes forward and backward on chapters" in {
    for {
      jud <- book.chapters.find(_.abbr == "Jud")
      ap <- book.chapters.find(_.abbr == "Ap")
      rdz <- book.chapters.find(_.abbr == "Rdz")
      wj <- book.chapters.find(_.abbr == "Wj")

    } {
      book.nextChapter(jud) should be (ap)
      book.nextChapter(ap) should be (rdz)
      book.prevChapter(wj) should be (rdz)
      book.prevChapter(rdz) should be (ap)
    }
  }

  it should "goes forward and backward on sources" in {
    for {
      apV0F0 <- book.sourcesV(book.versions(0)).find(_.chapter.abbr == "Ap")
      rdzV0F0 <- book.sourcesV(book.versions(0)).find(_.chapter.abbr == "Rdz")
      wjV0F0 <- book.sourcesV(book.versions(0)).find(_.chapter.abbr == "Wj")
    } {
      book.nextSource(apV0F0) should be (rdzV0F0)
      book.nextSource(rdzV0F0) should be (wjV0F0)
      book.prevSource(wjV0F0) should be (rdzV0F0)
      book.prevSource(rdzV0F0) should be (apV0F0)
    }
    val rdzAll = book.sourcesV(book.versions(1)).filter(_.chapter.abbr == "Rdz")
    val apAll = book.sourcesV(book.versions(1)).filter(_.chapter.abbr == "Ap")
    book.nextSource(rdzAll(0)) should be (rdzAll(1))
    book.nextSource(apAll.last) should be (rdzAll(0))
    book.prevSource(apAll(1)) should be (apAll(0))
    book.prevSource(rdzAll(0)) should be (apAll.last)
  }

  it should "return corect ref for audioSource" in {
    val rdz0 = book.sourcesCV(book.chapters(0))(book.versions(1))(0)
    val rdz1 = book.sourcesCV(book.chapters(1))(book.versions(1))(1)
    book.getRef(rdz0) should equal(new BookReference(book.isbn, 0,1,0))
    book.getRef(rdz1) should equal(new BookReference(book.isbn, 1,1,1))
  }

  it should "return corect ref for chapter" in {
    val rdz = book.chapters(0)
    val wj = book.chapters(1)
    book.getRef(rdz) should equal(new BookReference(book.isbn, 0))
    book.getRef(wj) should equal(new BookReference(book.isbn, 1))
  }

  it should "return corect ref for chapterVersion" in {???}

  "AudioSource" should "have properly set AudioSource times" in {
    for {
      wj <- book.sourcesV(book.versions(0)).find(_.chapter.abbr == "Wj")
      wjShort <- book.sourcesV(book.versions(1)).find(_.chapter.abbr == "Wj")
    } {
      wj.time should be (10654000L)
      wjShort.time should be (135000L)
    }
  }

  it should "can be pulled from cache" in {
    val rdz0 = book.sourcesCV(book.chapters(0))(book.versions(1))(0)
    val progress = new CacheStore[String,Int](0)
    rdz0.pull(progress) should be(0)
  }

  it should "can be pushed to cache" in {
    val rdz0 = book.sourcesCV(book.chapters(0))(book.versions(1))(0)
    val progress = new CacheStore[String,Int](0)
    rdz0.push(1)(progress)
    rdz0.pull(progress) should be(1)
  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }
}
