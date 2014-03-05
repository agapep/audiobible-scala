package com.agapep.model

import java.util.{Observer, Observable}
//import com.agapep.audiobible.model.logger.L


/**
 * Created by slovic on 05.03.14.
 */
object CacheManager {
  val TAG = "CacheManager"
  //define states
  val STATE_UNDOWNLOADED: Int = 2
  val STATE_DOWNLOADED: Int = 3
  val STATE_DOWNLOADING: Int = 4
  val STATE_ERROR: Int = 6
  val STATE_CANCEL: Int = 7
  val STATE_DOWNLOADED_PARTIALY: Int = 9
}

abstract class CacheManager extends Observable {
  import CacheManager._

  val progressObservable: ProgressObservable = new ProgressObservable
  val logger: Boolean = true

  /**
   * @param ref referencja do pliku audio! tylko!!!
   * @return jedna z predefiniowanych stałych (STATE_UNDOWNLOADED, STATE_DOWNLOADED, STATE_DOWNLOADING
   *         STATE_ERROR, STATE_CANCEL, STATE_DOWNLOADED_PARTIALY) zapisanych za pomocą setState(ref)
   */
  def getState(ref: BookReference2): Int

  def setState(ref: BookReference2, state: Int) {
//    if (logger) {
//      if (state == STATE_DOWNLOADED) L.og(c, L.FLAG_FINISH_DOWNLOAD, ref)
//      else if (state == STATE_DOWNLOADING) L.og(c, L.FLAG_START_DOWNLOAD, ref)
//      else if (state == STATE_CANCEL) L.og(c, L.FLAG_CANCEL, ref)
//      else if (state == STATE_ERROR) L.og(c, L.FLAG_ERROR_DOWNLOADING, ref)
//    }
    notifyObservers(ref)
  }

  def getProgress(ref: BookReference2): Int

  /**
   * zapisuje do bazy danych progress pobierania pliku oznaczonego przez ref.
   * @return czy jest inny niż ten który jest zapisany do tej pory.
   *         (jeśli nie to nie potrzeba odświerzać widoków)
   */
  def setProgress(ref: BookReference2, progress: Int): Boolean = {
    progressObservable.notifyObservers(ref)
    return true
  }

  /**
   * czyści wszystkie dane PŚ
   * @param b  instancja PŚ z którego chcemy wyczyścić dane
   */
  def clear(b: Book) {
    val ref: BookReference2 = b.start
//    L.og(c, L.FLAG_DELETE_ALL_FILES, ref)
    notifyObservers(ref)
  }

  /**
   * czyści dane dotyczące ref.
   * @param b instancja PŚ z którego chcemy wyczyścić dane
   * @param ref miejsce które chcemy wyczyścić.
   */
  def clear(b: Book, ref: BookReference2) {
//    L.og(c, L.FLAG_DELETE_FILE, ref)
    notifyObservers(ref)
  }

  def getAudioRef(b: Book, ref: BookReference2): BookReference2 = {
    return ref
  }

  def addObserver(observer: Observer, notifyProgress: Boolean) {
    this.addObserver(observer)
    if (notifyProgress) progressObservable.addObserver(observer)
  }

  def deleteObserver(observer: Observer, notifyProgress: Boolean) {
    super.deleteObserver(observer)
    if (notifyProgress) progressObservable.deleteObserver(observer)
  }

  override val hasChanged: Boolean = true
}

class ProgressObservable extends Observable {
  override def hasChanged: Boolean = return true
  override def toString: String = return CacheManager.TAG + "_PROGRESS"
}