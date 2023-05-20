package useCase.concurrent.prodicerConsumer.simple

import ProducerConsumer.{consumer, producer}
import cats.effect._
import cats.effect.std.Console
import cats.syntax.all._

import scala.collection.immutable.Queue

/**
 * This solution is a basic implementation of concurrency problem of
 * producer and consumer. This solution is inefficient because producers runs faster than the consumer
 * so the queue is constantly growing.
 *
 * To terminate the execution use CTRL-C
 */

object ProducerConsumerExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      queue <- Ref.of[IO, Queue[Int]](Queue.empty[Int])
      res <- (consumer(queue), producer(queue, 0))
        .parMapN((_, _) => ExitCode.Success) // Run producer and consumer in parallel until done
        .handleErrorWith { t =>
          Console[IO].errorln(s"Error caught: ${t.getMessage}").as(ExitCode.Error)
        }
    } yield res
  }
}
/*
Basic solution, but without error prevention
override def run(args: List[String]): IO[ExitCode] =
  for {
    queueR <- ref.of[IO, Queue[Int]](Queue.empty[Int])
    producerFiber <- producer(queueR, 0).start
    consumerFiber <- consumer(queueR).start
    _ <- producerFiber.join
    _ <- consumerFiber.join
  } yield ExitCode.Error
 */




