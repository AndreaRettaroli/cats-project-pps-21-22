package snippets.countdownlatch

import cats.implicits.*
import cats.effect.{IOApp, *}
import cats.effect.std.CountDownLatch
import cats.effect.unsafe.implicits.global

object CountDownLatchExample extends IOApp.Simple {
  override def run: IO[Unit] = {
    for {
      latch <- CountDownLatch[IO](3)
      fiber <- (latch.await >> IO.println("Latch unlocked")).start
      _ <- latch.release >> IO.println("First release")
      _ <- latch.release >> IO.println("Second release")
      _ <- latch.release >> IO.println("Third release")
      _ <- fiber.join
    } yield ()
  }
}
