package io.github.danderse.toplnewsapi

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object NewsEventRoutes {

  def routes[F[_]: Sync](source: NewsEventSource[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    object NumArticlesQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("num_articles")
    object KeywordsQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("keywords")

    HttpRoutes.of[F] {
      case GET -> Root / "news" :? NumArticlesQueryParamMatcher(maybeNumArticles) :? KeywordsQueryParamMatcher(maybeKeywords) =>
        for {
          events <- source.get(maybeNumArticles, maybeKeywords)
          resp <- Ok(events)
        } yield resp
    }
  }
}