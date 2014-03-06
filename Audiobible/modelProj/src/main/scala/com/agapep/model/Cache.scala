package com.agapep.model
import java.io.File

/**
 * Created by slovic on 13.02.14.
 *
 */
trait Cache {
  val path: File
  private val cache = collection.mutable.Map[Cacheable, File]()

  //return file connected to cached item.
  //you can write or read them.
  def cacheFile(item:Cacheable) = cache.getOrElseUpdate(item, new File(path, item.path))
  def existsFile(item:Cacheable) = cacheFile(item).exists()
}

trait Cacheable {
  def path: String
}