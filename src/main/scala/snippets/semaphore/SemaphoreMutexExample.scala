package snippets.semaphore

import cats.effect.{ IO, IOApp, Temporal }
import cats.effect.std.{ Console, Semaphore }
import cats.implicits.*
import cats.effect.syntax.all.*

import scala.concurrent.duration.*

object SemaphoreMutexExample extends IOApp.Simple {

  class PreciousResource[F[_]: Temporal](name: String, s: Semaphore[F])(implicit F: Console[F]) {

    def use: F[Unit] =
      for {
        x <- s.available
        _ <- F.println(s"$name >> Availability: $x")
        _ <- s.acquire // critical section
        y <- s.available
        _ <- F.println(s"$name >> Started | Availability: $y")
        _ <- s.release.delayBy(3.seconds) // end of critical session
        z <- s.available
        _ <- F.println(s"$name >> Done | Availability: $z")
      } yield ()
  }

  override def run: IO[Unit] =
    for {
      s <- Semaphore[IO](1) // mutex case, only one can access
      r1 = new PreciousResource[IO]("R1", s)
      r2 = new PreciousResource[IO]("R2", s)
      r3 = new PreciousResource[IO]("R3", s)
      _ <- List(r1.use, r2.use, r3.use).parSequence.void
    } yield ()

}
