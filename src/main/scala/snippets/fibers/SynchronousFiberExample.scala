package snippets.fibers

import cats.effect.{ IO, IOApp }

object SynchronousFiberExample extends IOApp.Simple {

  override val run: IO[Unit] =
    IO(Thread.sleep(1000)) >> IO.println(
      "Hello World"
    ) // Suspend current fiber for 1s then print "Hello World"
}
