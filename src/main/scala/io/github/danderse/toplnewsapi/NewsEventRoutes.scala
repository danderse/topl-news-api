package io.github.danderse.toplnewsapi

import cats.effect.Sync
import cats.implicits._
import org.http4s.circe._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._
import io.circe.literal._

object NewsEventRoutes {

  def routes[F[_]: Sync](source: NewsEventSource[F], newsEventCache: NewsEventCache[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    object NumArticlesQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("num_articles")
    object KeywordsQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("keywords")

    HttpRoutes.of[F] {
      case GET -> Root / "news" :? NumArticlesQueryParamMatcher(numArticles) :? KeywordsQueryParamMatcher(keywords) =>
        val (cacheHits, numArticlesRemaining) = newsEventCache.queryCache(numArticles, keywords)
        val clientResp = for {
          sourceResp <- source.get(numArticlesRemaining, keywords, cacheHits)
          _ <- newsEventCache.updateCache(sourceResp.articles)
        } yield NewsArticles(articles = sourceResp.articles.map(MetadataService.appendWordFrequencyToEvent))
        Ok(clientResp)
          .handleErrorWith(
            error => InternalServerError(json"""{ "error": ${error.getMessage().asJson}}""")
          )
    }
  }
}