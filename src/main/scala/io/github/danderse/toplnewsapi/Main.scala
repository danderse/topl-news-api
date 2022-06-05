package io.github.danderse.toplnewsapi

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    ToplnewsapiServer.stream[IO].compile.drain.as(ExitCode.Success)
}
