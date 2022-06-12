package io.github.danderse.toplnewsapi

import cats.effect.{ExitCode, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    NewsEventServer.stream.compile.drain.as(ExitCode.Success)
}
