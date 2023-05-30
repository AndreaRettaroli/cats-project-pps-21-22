package useCase.concurrent.diningPhilosophers

import cats.effect.std.{Console, Semaphore}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.*
import Philosopher.Philosopher

import scala.concurrent.duration.DurationInt

object DiningPhilosophersExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val numPhilosophers = 5
    for {
      s <- Semaphore[IO](1)
      forks = List.fill(numPhilosophers)(s)
      philosophers = (0 until numPhilosophers - 1).map { i =>
        new Philosopher(
          i,
          forks(i),
          forks((i + 1) % numPhilosophers)
        ) // id, leftFork, rightFork
      }
      lastPhilosopher = new Philosopher(
        numPhilosophers - 1,
        forks(0),
        forks(numPhilosophers - 1)
      )

      res <- (philosophers :+ lastPhilosopher)
        .map(_.dine)
        .toList
        .parSequence
        .as(
          ExitCode.Success
        )
        .handleErrorWith { t =>
          Console[IO]
            .errorln(s"Error caught: ${t.getMessage}")
            .as(ExitCode.Error)
        }
    } yield res
  }
}
