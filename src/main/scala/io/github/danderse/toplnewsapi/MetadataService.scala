package io.github.danderse.toplnewsapi

import scala.annotation.tailrec
import scala.collection.immutable.HashMap

object MetadataService {
        
  def updateFrequency(word: String, map: Map[String, Int]): Map[String, Int] = {
    map.get(word) match {
      case Some(count) => map + (word -> (count + 1))
      case None => map + (word -> 1)
    }
  }

  @tailrec
  def calculateWordFrequency(words: Seq[String], map: Map[String, Int]): Map[String, Int] = {
    words match {
      case Nil => map
      case head :: Nil => updateFrequency(head, map)
      case head :: tail => calculateWordFrequency(tail, updateFrequency(head, map))
    }
  }

  def appendWordFrequencyToEvent(event: NewsEvent): NewsEvent = {
    val wordFrequencyMap = event.content.map(c =>
      calculateWordFrequency(c.split("\\s").toSeq, new HashMap[String, Int]())
    )
    event.copy(wordFrequencyMap = wordFrequencyMap)
  }
}