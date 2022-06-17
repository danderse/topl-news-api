package io.github.danderse.toplnewsapi

import cats.effect.IO
import scala.collection.immutable.HashSet

trait NewsEventCache[F[_]] {
  def updateCache(events: List[NewsEvent]): F[Unit]
  def queryCache(numArticles: Option[Int], keywords: Option[String]): (List[NewsEvent], Option[Int])
}

object NewsEventCache {
  def apply(cacheMax: Int) = new NewsEventCache[IO] {
    var cache = new HashSet[NewsEvent]()

    def updateCache(events: List[NewsEvent]) =
      IO (
        events.foreach(event => {
          if (cache(event)) ()
          else {
            if (cache.size < cacheMax) cache = cache.union(Set(event))
            else cache = cache.drop(1).union(Set(event))
          }
        })
      )

    def queryCache(numArticles: Option[Int], keywords: Option[String]) = {
      // just support basic search for now, union on comma-delimited keywords
      val resultSet = keywords match {
        case Some(words) => {
          val wordSet = words.split(",").toSet
          cache.filter(event =>
            wordSet
              .map(w => event.title.contains(w) || event.description.contains(w) || event.content.contains(w))
              .reduce(_ || _)
          )
        }
        case None => cache
      }

      // return only the num specified matching words
      // or if the size of the result set is smaller, return the difference as the number to be requested by the API
      numArticles match {
        case Some(n) if n >= resultSet.size => (resultSet.toList, Some(n - resultSet.size))
        case Some(n) => (resultSet.toList.take(resultSet.size - n), Some(0))
        case None => (resultSet.toList, None)
      }
    }
  }
}
