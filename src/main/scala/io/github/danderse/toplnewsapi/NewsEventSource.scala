package io.github.danderse.toplnewsapi

trait NewsEventSource[F[_]]{
  def get(numArticles: Option[Int], keywords: Option[String], cacheHits: List[NewsEvent]): F[NewsArticles]
}

object NewsEventSource { 
  def apply[F[_]](implicit ev: NewsEventSource[F]): NewsEventSource[F] = ev
}
