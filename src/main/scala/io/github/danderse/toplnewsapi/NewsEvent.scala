package io.github.danderse.toplnewsapi

import cats.effect.Concurrent
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import java.time.ZonedDateTime

final case class NewsEvent(
    title: Option[String],
    description: Option[String],
    content: Option[String],
    image: Option[String],
    url: Option[String],
    publishedAt: Option[ZonedDateTime],
    wordFrequencyMap: Option[Map[String, Int]],
    source: EventSource
  )

final case class EventSource(id: Option[String], name: Option[String])

object NewsEvent {
  implicit def newsEventEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewsEvent] =
    jsonOf
  implicit def newsEventEntityEncoder[F[_]]: EntityEncoder[F, NewsEvent] =
    jsonEncoderOf
}

final case class NewsEventError(e: Throwable) extends RuntimeException

final case class NewsArticles (articles: List[NewsEvent])

object NewsArticles {
  implicit def newsArticlesEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewsArticles] =
    jsonOf
  implicit def newsArticlesEntityEncoder[F[_]]: EntityEncoder[F, NewsArticles] =
    jsonEncoderOf
}
