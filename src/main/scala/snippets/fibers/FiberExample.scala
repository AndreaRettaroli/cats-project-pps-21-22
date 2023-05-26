package snippets.fibers

import cats.effect.kernel.Outcome
import cats.effect.{ ExitCode, Fiber, FiberIO, IO, IOApp }
import cats.implicits.*

import scala.concurrent.duration.DurationInt

object FiberExample extends IOApp {

  val intValue: IO[Int] = IO(1)
  val stringValue: IO[String] = IO("Scala")

  implicit class Extension[A](io: IO[A]) {

    def printThread: IO[A] =
      io.map { value =>
        println(s"[${Thread.currentThread().getName}] $value")
        value
      }
  }

  def sameThread(): IO[Unit] = for {
    _ <- intValue.printThread
    _ <- stringValue.printThread
  } yield ()
  /*
  I tre parametri generici sono:
  - Tipo dell'effetto: in questo caso IO
  - Il tipo dell'errore su cui potrebbe fallire: Throwable
  - Il tipo di dato che ritornerebbe in caso di successo: Int
   */
  val fiber: IO[Fiber[IO, Throwable, Int]] = intValue.printThread.start

  def differentThread(): IO[Unit] =
    for {
      _ <- fiber
      _ <- stringValue.printThread
    } yield ()

  def runOnAnotherThread[A](io: IO[A]): IO[Outcome[IO, Throwable, A]] =
    for {
      fib <- io.start // fiber
      result <- fib.join
      /*
        1 - success(IO(value))
        2 - errored(e)
        3 - cancelled
       */
    } yield result

  def throwOnAnotherThread(): IO[Outcome[IO, Throwable, Int]] =
    for {
      fib <- IO.raiseError[Int](new RuntimeException("Error")).start
      result <- fib.join
    } yield result

  def cancelOnAnotherThread(): IO[Outcome[IO, Throwable, String]] = {
    val task = IO("starting").printThread *> IO.sleep(1.second) *> IO("done").printThread
    for {
      fib <- task.start
      _ <- IO.sleep(500.millis) *> IO("cancelling").printThread
      _ <- fib.cancel
      result <- fib.join
    } yield result
  }

  override def run(args: List[String]): IO[ExitCode] =
    // intValue.printThread *> stringValue.printThread *> IO(ExitCode.Success)
    // sameThread().as(ExitCode.Success)
    // differentThread().as(ExitCode.Success)
    // runOnAnotherThread(intValue).printThread.as(ExitCode.Success)
    // throwOnAnotherThread().printThread.as(ExitCode.Success)
    cancelOnAnotherThread().printThread.as(ExitCode.Success)
}
