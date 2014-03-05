package com.agapep.model
import java.io.File

/**
 * Created by slovic on 13.02.14.
 *
 */
trait Cache {
  val path: File
  private val cache = collection.mutable.Map[Cacheable, File]()

  def cacheFile(item:Cacheable) =
    cache.getOrElseUpdate(item, new File(path, item.path))
  def exists(item:Cacheable) = cacheFile(item).exists()
}

trait Cacheable {
  def path: String
}