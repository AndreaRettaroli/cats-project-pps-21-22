package useCase.concurrent.diningPhilosophers

import cats.effect.std.{Semaphore}
import cats.effect.{IO}
import cats.implicits.*

import scala.concurrent.duration.DurationInt

object Philosopher {

  class Philosopher(
      val id: Int,
      val leftFork: Semaphore[IO],
      val rightFork: Semaphore[IO]
  ) {
    def think: IO[Unit] =
      IO(println(s"Philosopher $id is thinking")) *> IO.sleep(2.seconds)

    def eat: IO[Unit] = IO(println(s"Philosopher $id is eating")) *>
      IO.sleep(1.seconds) *> IO(println(s"Philosopher $id end eating"))

    def acquireForks: IO[Unit] =
      for {
        _ <- leftFork.tryAcquire.ifM(
          IO(println(s"Philosopher $id acquired left")) *>
            rightFork.tryAcquire.ifM(
              IO(println(s"Philosopher $id acquired right with left")), //true case
              IO(println(s"Philosopher $id release left")) *> leftFork.release //false case
            ),
          acquireForks
        )
      } yield ()

    def releaseForks: IO[Unit] =
      for {
        _ <- IO(println(s"Philosopher $id release left")) *> leftFork.release
        _ <- IO(println(s"Philosopher $id release right")) *> rightFork.release

      } yield ()

    def dine: IO[Unit] =
      (think *> acquireForks *> eat *> releaseForks).foreverM
  }
}
