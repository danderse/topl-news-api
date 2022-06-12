package io.github.danderse.toplnewsapi

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import cats.effect.IO
import org.http4s.server.middleware.Logger

object NewsEventServer {

  def stream: Stream[IO, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[IO].build)
      newsEventSource = NewsApi(client)

      httpApp = (
        NewsEventRoutes.routes[IO](newsEventSource)
      ).orNotFound

      // With Middlewares in place
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder.default[IO]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
        Resource.eval(Async[IO].never)
      )
    } yield exitCode
  }.drain
}
