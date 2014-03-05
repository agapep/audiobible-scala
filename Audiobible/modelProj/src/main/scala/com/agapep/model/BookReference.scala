package com.agapep.model

object BookReference {
  val TYPE_BOOK: Int = 0
  val TYPE_CHAPTER: Int = 1
  val TYPE_VERSION: Int = 2
  val TYPE_AUDIO: Int = 3
  val TYPE_AUDIO_POINT: Int = 4
  val TYPE_AUDIO_PART: Int = 5

  //tested
  def apply(ref: String): BookReference = {
    implicit def string2Int(s: String): Int = augmentString(s).toInt
    implicit def string2Long(s: String): Long = augmentString(s).toLong
    def getOrElse[T](list: Array[T],pos: Int , default: T = "-1"): T = if (list.length > pos) list(pos) else default

    val values: Array[String] = ref.split("/")
    assert(values.length <= 6, "BookReference string shoud not have more than 6 elems.")
    new BookReference(BigInt(values(0)),
      getOrElse(values, 1), getOrElse(values, 2),
      getOrElse(values, 3), getOrElse(values, 4), getOrElse(values, 5))
  }
}

case class BookReference(
         isbn: BigInt,
         chapter: Int,
         chapterVersion: Int = -1,
         fileId: Int = -1,
         time: Long = -1,
         timeEnd: Long = -1 ) {
  import BookReference._
  //tested
  val refType = {
    if (chapterVersion < 0) TYPE_CHAPTER
    else if (fileId < 0) TYPE_VERSION
    else if (time < 0)  TYPE_AUDIO
    else if (timeEnd < 0) TYPE_AUDIO_POINT
    else TYPE_AUDIO_PART
  }


  override lazy val toString: String = {
    val completeList = isbn :: chapter :: {
      refType match {
        case TYPE_CHAPTER => Nil
        case TYPE_VERSION => chapterVersion :: Nil
        case TYPE_AUDIO => chapterVersion :: fileId :: Nil
        case TYPE_AUDIO_POINT => chapterVersion :: fileId :: time :: Nil
        case TYPE_AUDIO_PART => chapterVersion :: fileId :: time :: timeEnd :: Nil
      }
    }
    completeList.mkString("/")
  }


  def isChapterReference: Boolean = refType >= TYPE_CHAPTER //tested
  def isVersionReference: Boolean = refType >= TYPE_VERSION //tested
  def isAudioReference: Boolean = refType >=TYPE_AUDIO //tested

  def toChapterReference = new BookReference(isbn, chapter) //
  def toFileReference = new BookReference(isbn, chapter, chapterVersion, fileId)
  lazy val toAudioSourceFileString = toFileReference.toString //TODO mało wydajnie

  //TODO test
  def fileEquals(last: BookReference): Boolean = {
    if (last == null) return false
    else last.toFileReference.equals(this.toFileReference)
  }
}