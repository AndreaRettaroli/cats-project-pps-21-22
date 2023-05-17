package snippets.ref

import cats.effect.{ExitCode, IO, IOApp, Sync, Ref}
import cats.syntax.all.*
import cats.implicits._

object RefExample extends IOApp.Simple {
  class Worker[F[_]](id: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {

    private def putStrLn(value: String): F[Unit] =
      F.blocking(println(value))

    def start: F[Unit] =
      for {
        c1 <- ref.get
        _ <- putStrLn(show"Worker #$id >> $c1")
        c2 <- ref.updateAndGet(x => x + 1)
        _ <- putStrLn(show"Worker #$id >> $c2")
      } yield ()
  }

  override def run: IO[Unit] = {
    for {
      ref <- Ref[IO].of(0)
      w1 = new Worker[IO](1, ref)
      w2 = new Worker[IO](2, ref)
      w3 = new Worker[IO](3, ref)
      _ <- List(w1.start, w2.start, w3.start).parSequence.void
    } yield ()
  }
}
