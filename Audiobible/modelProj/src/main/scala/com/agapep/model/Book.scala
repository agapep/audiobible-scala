package com.agapep.model

import com.agapep.model.{Book, MeasureTime}
import java.io.{File, InputStream}
import scala.collection.immutable.Nil
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Try

/**
 * Created by slovic on 11.02.14.
 */
object Book {
  import CsvBookConst._

  def apply(info: BookInfo, rawData: List[String]):Book = {
    val (tomes, chapters, chapterTypes , chapterVersions , audioSources) = readFromCsvData(info, rawData)
    new Book(info, tomes, chapters, chapterTypes , chapterVersions , audioSources)
  }

  //This function work similar to split on string. Partition list on lines witch not implement(?) predicate.
  // Elem this is on top in one of returned lists.
  private def splitOn[A](f: A => Boolean, data:List[A], result: Map[A, List[A]] = Map[A, List[A]]()) : Map[A,List[A]] = {
    if (data.isEmpty) return Map()
    if (!data.tail.forall(f)) {
      val span = data.tail.span(f)
      return Map(data.head -> span._1) ++ splitOn(f, span._2, result)
    } else {
      return Map(data.head -> data.tail) ++ result
    }
  }

  private def timeMapper(timeStr: String) = {
    val timeZip = SECONDS ::  MINUTES :: HOURS :: Nil
    timeStr.split(":").map(_.trim.toInt).reverse.zip(timeZip).map(i => Duration(i._1, i._2)).reduceLeft(_ + _).toMillis
  }

  def readFromCsvData(book: BookInfo, data:List[String]):
  (List[Tome], List[Chapter], List[ChapterType], List[ChapterVersion], List[AudioSource]) = {
    //-----------------------------------------------
    //---- create raw lists list of audio_source-----
    //-----------------------------------------------

    //-------raw list: audio_source and tomes -------
    val raw :List[List[String]] = (data map { line: String =>
      val lineSeq = line.split(";") toList;
      lineSeq match {
        case Nil => Nil
        case "" :: Nil => Nil
        case (s:String) :: Nil => lineSeq
        case  url :: chapterName :: abbr :: versionName :: sourceName :: time :: chapterType :: Nil => lineSeq
      }
    }).filter(_ != Nil).toList

    //---------raw map: tomes -> sources----------------
    val tomesRaw: Map[List[String], List[List[String]]] = {
      def f[A](x:A):Boolean =  x match {
        case (s:String) :: Nil => false
        case x => true
      }
      splitOn(f, raw)
    }
    //>>>>>>>>>> print <<<<<<<<<<
    // println("\ntomesRaw:"); tomes.foreach(println)

    //-----------------------------------------------
    //-------create usefull lists -------------------
    //-----------------------------------------------

    //---------list: chapters -----------------------
    val chapters = {
      tomesRaw.map { tomePack => // (tomeName:List[Str] , sourcesInTome: List[List[Str]])
        val tome = Tome(tomePack._1.head)
        val sources:List[List[String]] = tomePack._2
        sources.map(                                      //Mapujemy liste źródeł
          l => (l(CHAPTER_NAME), l(ABBR), ChapterType(l(CHAPTER_TYPE)))).  //na interesujące nas krotki
          distinct.                                       //Wybieramy tylko niepowtarzające się
          map(l => Chapter(book, tome, l._1, l._2, l._3)) //konstruujemy pliki rozdziałów
      }.foldLeft[List[Chapter]](Nil)(_ ::: _)
    }
    //>>>>>>>>>> print <<<<<<<<<<
    //println("\nchapters:"); chapters foreach { ch => println( ch.tome.name +": "+ ch.name ) }

    //---------list: versions-----------------------
    val versions = raw.filter(_.length >= CSV_SIZE).map(c => ChapterVersion(c(VERSION_NAME))).distinct
    //>>>>>>>>>> print <<<<<<<<<<
    //println("\nversions:"); versions foreach { v => println( v.name ) }

    val sources = {
      val sourcesList = raw.filter(_.length >= CSV_SIZE)
      for {
        l <- sourcesList
        chapter <- chapters.find(_.name == l(CHAPTER_NAME))
        version  <- versions.find(_.name == l(VERSION_NAME))
      } yield AudioSource(chapter, version, l(URL) , l(SOURCE_NAME), timeMapper(l(TIME)))
    }
    //>>>>>>>>>> print <<<<<<<<<<
    //println("\nsources:"); sources foreach { s => println( s.chapter.abbr , s.version.name, s.name ) }

    val tomes = chapters.map(_.tome).distinct

    val types = chapters.map(_.`type`).distinct
    //>>>>>>>>>> print <<<<<<<<<<
    //println("\ntomes:"); tomes foreach { t => println( t.name ) }

    return (tomes, chapters, types, versions, sources)
  }
}

class Book(
  val info: BookInfo, //podstawowe informacje na temat książki.
  val tomes: List[Tome] , //lista tomów.
  val chapters: List[Chapter], //Lista rozdziałów
  val chapterTypes:List[ChapterType], // typy rozdziałów (w pś będą to księgi historyczne/ mądrościowe)
  val versions:List[ChapterVersion], // lista wersji plików audio. Domyślnie są dwie cała/podzielona
  val sources: List[AudioSource] //lista plików audio
  ) extends Cache {
    override val path: File = new File("/home/slovic/IdeaProjects/AudioBibleScala/src/main/res/")
    lazy val start = new BookReference(isbn,0,0,0)
    val isbn: BigInt = info.isbn
    val  name: String = info.name

    //helpers. provide list
    lazy val sourcesV = sources.groupBy(_.version)
    lazy val sourcesC = sources.groupBy(_.chapter)
    lazy val sourcesCV = sourcesC.map(sC => (sC._1, sC._2.groupBy(_.version)) )

    private def next[A](col: List[A], elem:A): A = {
      val pos = col.indexOf(elem) + 1
      col( pos % col.length)
    }

    private def prev[A](col: List[A], elem:A): A = {
      val elemId = col.indexOf(elem)
      val pos = if (elemId > 0) elemId - 1 else col.length-1
      col( pos )
    }

    def nextChapter(c: Chapter) = next(chapters, c)
    def prevChapter(c: Chapter) = prev(chapters, c)

    def nextSource(s: AudioSource) = next(sourcesV(s.version), s)
    def prevSource(s: AudioSource) = prev(sourcesV(s.version), s)

    def getRef(c: Chapter):BookReference = new BookReference(
      c.book.isbn,
      chapters.indexOf(c))

  /**
   * W niektórych przypadkach będziemy używać pliku referencji do manipulowania
   * na danych. Pliki ref mogą w łatwy sposób być rzutowane na AudioSource, Chapter
   * ChapterVersion TODO auto
   * @param s opis pliku audio
   * @return plik referencji do wybranego pliku audio
   */
    def getRef(s: AudioSource):BookReference = getRef(s.chapter).copy(
      chapterVersion = versions.indexOf(s.version),
      fileId = sourcesCV(s.chapter)(s.version).indexOf(s))
  }

case class BookInfo (val isbn: BigInt,val  name: String)

//Tomy są po to aby odróżniać
case class Tome(name: String)

case class Chapter(book: BookInfo, tome: Tome , name: String, abbr: String, `type`:ChapterType=ChapterType() )

//ChapterVersion jest po to aby odróżniać
case class ChapterVersion(name:String)

case class ChapterType(name:String = "")

case class AudioSource(chapter: Chapter, version: ChapterVersion, url: String, name: String, time: Long)
  extends Cacheable with Cached {
  override lazy val path: String = List[String](chapter.book.isbn.toString, chapter.abbr, version.name, name).
    mkString("/") + ".mp3"
}

object CsvBookConst {
  val URL = 0
  val CHAPTER_NAME = 1
  val ABBR = 2
  val VERSION_NAME = 3
  val SOURCE_NAME = 4
  val TIME = 5
  val CHAPTER_TYPE = 6
  val CSV_SIZE = 7
}