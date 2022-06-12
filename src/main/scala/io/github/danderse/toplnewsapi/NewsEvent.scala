package io.github.danderse.toplnewsapi

import cats.effect.Concurrent
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import java.time.OffsetDateTime

final case class NewsEvent(
    title: String,
    description: String,
    content: String,
    image: Option[String],
    url: String,
    publishedAt: OffsetDateTime,
    wordFrequencyMap: Option[Map[String, Int]],
    source: EventSource
  )

final case class EventSource(id: String, name: String)

object NewsEvent {
  implicit def newsEventEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewsEvent] =
    jsonOf
  implicit def newsEventEntityEncoder[F[_]]: EntityEncoder[F, NewsEvent] =
    jsonEncoderOf
}

final case class NewsEventError(e: Throwable) extends RuntimeException