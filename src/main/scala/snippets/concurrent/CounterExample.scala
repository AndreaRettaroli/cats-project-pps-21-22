package snippets.concurrent

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._

object CounterExample extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      state <- IO.ref(0)
      fibers <- state.update(_ + 1).start.replicateA(100)
      _ <- fibers.traverse(_.join).void
      value <- state.get
      _ <- IO.println(s"The final value is: $value") // => The final value is: 100
    } yield ExitCode.Success
}
