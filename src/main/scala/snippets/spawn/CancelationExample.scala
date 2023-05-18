package snippets.spawn

import scala.concurrent.duration.*
import cats.effect.{ExitCode, IO, IOApp}

object CancelationExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      target <- IO.println("Catch me if you can!").foreverM.start
      _ <- IO.sleep(1.second)
      _ <- target.cancel
    } yield ExitCode.Success
  }
}
