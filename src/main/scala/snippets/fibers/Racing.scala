package snippets.fibers

import cats.effect._
import cats.effect.syntax.all._
import scala.concurrent.duration._

object Racing extends IOApp.Simple {

  val intValue: IO[Int] = IO(1)

  implicit class Extension[A](io: IO[A]) {
    def printThread: IO[A] = {
      io.map { value =>
        println(s"[${Thread.currentThread().getName}] $value")
        value
      }
    }
  }

  val valuableIO: IO[Int] = {
    IO("task starting").printThread *> IO.sleep(1.second).printThread >> IO(
      "task completed"
    ).printThread *> IO(1).printThread
  }
  val vIO: IO[Int] = valuableIO.onCancel(IO("task: cancelled").printThread.void)
  val timeout: IO[Unit] = {
    IO("timeout: starting").printThread >> IO.sleep(500.millis).printThread >> IO(
      "timeout: finished"
    ).printThread.void
  }

  def race(): IO[String] = {
    // The losing fiber gets canceled
    val firstIO: IO[Either[Int, Unit]] =
      IO.race(vIO, timeout) // IO.race => IO[Either[A, B]]

    firstIO.flatMap {
      case Left(v)  => IO(s"task won: $v")
      case Right(_) => IO("timeout won")
    }
  }

  val testTimeout: IO[Int] = vIO.timeout(500.millis)

  def racePair[A](ioA: IO[A], ioB: IO[A]): IO[OutcomeIO[A]] = {
    // Losing fiber does not get canceled
    val pair = IO.racePair(
      ioA,
      ioB
    ) // IO[Either[(OutcomeIO[A], FiberIO[B]), (FiberIO[A], OutcomeIO[B])]]

    pair.flatMap {
      case Left((outcomeA, fiberB)) =>
        fiberB.cancel *> IO("first task won").printThread *> IO(outcomeA).printThread
      case Right((fiberA, outcomeB)) =>
        fiberA.cancel *> IO("second task won").printThread *> IO(outcomeB).printThread
    }
  }

  val ioA: IO[Int] =
    IO.sleep(1.second).as(1).onCancel(IO("first cancelled").printThread.void)
  val ioB: IO[Int] =
    IO.sleep(2.second).as(2).onCancel(IO("second cancelled").printThread.void)

  def run: IO[Unit] = racePair(ioA, ioB).void//race().printThread.void or racePair(ioA, ioB).void
}
