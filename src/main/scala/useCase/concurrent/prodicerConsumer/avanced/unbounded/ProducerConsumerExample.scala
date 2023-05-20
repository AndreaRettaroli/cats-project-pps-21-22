package useCase.concurrent.prodicerConsumer.avanced.unbounded

import ProducerConsumer.{State, consumer, producer}
import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits.*

/**
 * This solution is an implementation of concurrency problem of
 * producer and consumer with unbounded buffer.
 * To terminate the execution use CTRL-C || CTRL+F2
 */

object ProducerConsumerExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      state <- Ref.of[IO, State[IO, Int]](State.empty[IO, Int])
      counter <- Ref.of[IO, Int](1)
      producers = List.range(1, 11).map(producer(_, counter, state)) //-> 10 producers
      consumers = List.range(1, 11).map(consumer(_, state)) //-> 10 consumers
      res <- (producers ++ consumers)
        .parSequence.as(ExitCode.Success) // Run producer and consumer in parallel until done (cancelling with CTRL-C)
        .handleErrorWith {
          t => Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    } yield res
  }
}



