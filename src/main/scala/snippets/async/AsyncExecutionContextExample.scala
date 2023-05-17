package snippets.async

import cats.effect.{Async, ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext

object AsyncExecutionContextExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val printThread: IO[Unit] =
      Async[IO].executionContext.flatMap(IO.println(_))

    for {
      _ <- printThread //WorkStealingThreadPool
      _ <- Async[IO].evalOn(
        printThread,
        ExecutionContext.global
      ) // ExecutionContextImpl$$anon$3@1f8881a1[Running, parallelism = 8, size = 1, active = 1, running = 1, steals = 0, tasks = 0, submissions = 0]
      _ <- printThread //WorkStealingThreadPool
    } yield ExitCode.Success

  }
}
