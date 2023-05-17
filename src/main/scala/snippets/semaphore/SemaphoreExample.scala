package snippets.semaphore

import cats.effect.{IO, IOApp, Sync}
import cats.effect.std.{Console, Semaphore}
import cats.syntax.all.*

object SemaphoreExample extends IOApp.Simple {

  class Reader[F[_]](name: String, semaphore: Semaphore[F])(implicit
      F: Sync[F]
  ) {
    def use: F[Unit] =
      semaphore.acquire >>
        F.delay(println(s"$name: Acquired")) >>
        F.delay(println(s"$name: Using resource")) >>
        F.delay(println(s"$name: Releasing")) >>
        semaphore.release
  }

  override def run: IO[Unit] = {
    for {
      s <- Semaphore[IO](2) //maximum 2 reader at time
      r1 = new Reader[IO]("R1", s)
      r2 = new Reader[IO]("R2", s)
      r3 = new Reader[IO]("R3", s)
      _ <- List(r1.use, r2.use, r3.use).parSequence.void
    } yield ()
  }
}
