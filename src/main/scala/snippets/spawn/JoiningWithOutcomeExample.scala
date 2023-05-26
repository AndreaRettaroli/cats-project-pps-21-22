package snippets.spawn

import cats.MonadError
import cats.effect.{ ExitCode, IO, IOApp, MonadCancel, Outcome, Spawn }

import scala.concurrent.duration.DurationInt

object JoiningWithOutcomeExample extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    for {
      fiberA <- (IO
        .println("Hello I am Fiber A")
        .foreverM
        .timeoutTo(3.seconds, IO.unit)
        >> IO.pure(1)).start
      fiberB <- (IO
        .println("Hello I am Fiber B")
        .foreverM
        .timeoutTo(3.seconds, IO.unit)
        >> IO.pure(1)).start

      /* Attendo il completamento */
      a <- fiberA.joinWithNever
      b <- fiberB.join flatMap {
        case Outcome.Succeeded(fb) => fb
        case Outcome.Errored(e)    => MonadError[IO, Throwable].raiseError(e)
        /* Nel caso in cui il fiber figlio venga interrotto, si cerca di
        interrompere l'attuale fiber, se non è possibile si cade in deadlock */
        case Outcome.Canceled() => MonadCancel[IO].canceled >> Spawn[IO].never
      }
      _ <- IO.println(s"a: $a, b: $b")
    } yield ExitCode.Success
}
