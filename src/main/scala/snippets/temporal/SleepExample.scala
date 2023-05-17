package snippets.temporal

import cats.effect.{ExitCode, IO, IOApp, Sync}

import scala.concurrent.duration.DurationInt

object SleepExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO.println("Before go sleep") >> Sync[IO].sleep(5.seconds) >> IO
        .println("After wake up!")
    } yield ExitCode.Success
  }
}
