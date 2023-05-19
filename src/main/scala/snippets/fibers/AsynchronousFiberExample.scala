package snippets.fibers

import cats.effect.{IO, IOApp}
import java.util.concurrent.{Executors, TimeUnit}

object AsynchronousFiberExample extends IOApp.Simple {
  val scheduler = Executors.newScheduledThreadPool(1)

  override def run: IO[Unit] = {
    IO.async_[Unit] { cb =>
      scheduler.schedule(
        new Runnable {
          def run = cb(Right(()))
        },
        500,
        TimeUnit.MILLISECONDS
      )
      ()
    }
  }

}
