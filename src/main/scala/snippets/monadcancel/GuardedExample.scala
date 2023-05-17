package snippets.monadcancel

import cats.effect.{IO, IOApp, MonadCancel}
import cats.effect.std.Semaphore
import cats.effect.syntax.all.*
import cats.syntax.all.*

object GuardedExample extends IOApp.Simple {
  def guarded[F[_], R, A, E](s: Semaphore[F], alloc: F[R])(
      use: R => F[A]
  )(release: R => F[Unit])(implicit F: MonadCancel[F, E]): F[A] =
    F uncancelable { poll =>
      for {
        r <- alloc

        _ <- poll(s.acquire).onCancel(release(r))
        releaseAll = s.release >> release(r)

        a <- poll(use(r)).guarantee(releaseAll)
      } yield a
    }
  def use(r: String): IO[Unit] = IO(println(s"Using resource: $r"))
  def release(r: String): IO[Unit] = IO(println(s"Releasing resource: $r"))

  override def run: IO[Unit] = {
    for {
      s <- Semaphore[IO](2)
      alloc <- IO("Resource")
      _ <- guarded(s, IO.pure(alloc))(use)(release)
    } yield ()
  }

}
