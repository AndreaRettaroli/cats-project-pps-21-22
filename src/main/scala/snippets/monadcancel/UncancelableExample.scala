package snippets.monadcancel

import cats.effect.{IO, IOApp}
import cats.effect.unsafe.implicits.global

object UncancelableExample extends IOApp.Simple {
  override def run: IO[Unit] = {
    for {
      fib <- (IO.uncancelable(_ => IO.canceled >> IO.println("Hello"))
        >> IO.println(" World!")).start
      res <- fib.join
    } yield res //print hello and cancel execution
  }

}
