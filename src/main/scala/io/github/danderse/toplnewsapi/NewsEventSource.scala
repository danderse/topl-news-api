package io.github.danderse.toplnewsapi

trait NewsEventSource[F[_]]{
  def get(numArticles: Option[Int], keywords: Option[String]): F[NewsEvent]
}

object NewsEventSource { 
  def apply[F[_]](implicit ev: NewsEventSource[F]): NewsEventSource[F] = ev // ??? does what now
}
