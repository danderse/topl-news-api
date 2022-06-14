package io.github.danderse.toplnewsapi

import io.circe.{Decoder, HCursor}
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import cats.effect.IO
import cats.effect.Concurrent
import java.time.ZonedDateTime

object NewsApi {

  implicit val newsArticlesDecoder: Decoder[NewsArticles] = new Decoder[NewsArticles] {
    def apply(c: HCursor): Decoder.Result[NewsArticles] = 
      c.downField("articles").as[List[NewsEvent]].map(NewsArticles(_))
  }
  
  implicit val newsEventDecoder: Decoder[NewsEvent] = new Decoder[NewsEvent] {
    final def apply(c: HCursor): Decoder.Result[NewsEvent] = {
      for {
        title <- c.downField("title").as[Option[String]]
        description <- c.downField("description").as[Option[String]]
        content <- c.downField("content").as[Option[String]]
        image <- c.downField("urlToImage").as[Option[String]]
        url <- c.downField("url").as[Option[String]]
        publishedAt <- c.downField("publishedAt").as[Option[String]].map(maybeDate => maybeDate.map(ZonedDateTime.parse(_)))
        sourceId <- c.downField("source").downField("id").as[Option[String]]
        sourceName <- c.downField("source").downField("name").as[Option[String]]
        source = new EventSource(sourceId, sourceName)
      } yield {
        new NewsEvent(title, description, content, image, url, publishedAt, None, source)
      }
    }
  }

  implicit def newsEventEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewsEvent] =
    jsonOf

  implicit def newsArticlesEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, NewsArticles] =
    jsonOf

  /*implicit val newsEventEncoder: Encoder[NewsEvent] = new Encoder[NewsEvent] {
    final def apply(event: NewsEvent): Json = Json.obj (
      ("title", Json.fromString(event.title)),
      ("description", Json.fromString(event.description)),
      ("content", Json.fromString(event.content)),
      ("image", Some(Json.fromString(event.image))),
      ("url", Some(Json.fromString))
    )
  }*/

  def constructSearchUri(numArticles: Option[Int], keywords: Option[String]): Uri = {
    val baseUri = "https://newsapi.org/v2/everything"
    val token = "4e82d6f5ddc347f2b9446e7fc92ea577"
    val numArticlesParam = numArticles.map(n => s"&pageSize=$n").getOrElse("")
    val keywordsParam = keywords.map(query => s"&q=$query").getOrElse("")
    val uriString = s"$baseUri?apikey=$token$keywordsParam$numArticlesParam"
    Uri.fromString(uriString) match {
      case Right(uri) => uri
      case _ => throw new RuntimeException(s"Failed to parse URI with base $baseUri and params $numArticlesParam, $keywordsParam")
    }
  }

  def apply(C: Client[IO]): NewsEventSource[IO] = new NewsEventSource[IO]{ //again, what is concurrent doing??
    val dsl = new Http4sClientDsl[IO]{}
    import dsl._

    def get(numArticles: Option[Int], keywords: Option[String]): IO[NewsArticles] = {
      for {
        uri <- IO(constructSearchUri(numArticles, keywords))
        resp <- C.expect[NewsArticles](GET(uri))
      } yield(resp)
    }
  }
}
