package snippets.fibers

import cats.effect.{IO, IOApp}
import cats.effect.cps._

object AsyncAwaitFiberExample extends IOApp.Simple {
  override def run: IO[Unit] =
    async[IO] {
      IO.println("Hello").await
      IO.println("World").await
    }
}

