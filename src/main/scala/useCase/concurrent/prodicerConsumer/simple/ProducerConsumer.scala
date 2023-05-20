package useCase.concurrent.prodicerConsumer.simple

import cats.effect.std.Console
import cats.effect.{Ref, Sync}
import cats.syntax.all._

import scala.collection.immutable.Queue
/*
ref: a ref instance allow to access to the data in safe mode, infact
when some fiber is runnning one of those methods, any other call to any method of the ref instance will be blocked.
 */

object ProducerConsumer{

  def producer[F[_]: Sync: Console](queue: Ref[F, Queue[Int]], counter: Int): F[Unit] = {
    for {
      _ <- if(counter % 10000 == 0) Console[F].println(s"Produced item $counter ") else Sync[F].unit
      _ <- queue.getAndUpdate(_.enqueue(counter + 1)) // Add element to the queue, only one fiber per time can access
      _ <- producer(queue, counter + 1)
    } yield ()
  }

  def consumer[F[_]: Sync: Console](queue: Ref[F, Queue[Int]]): F[Unit] = {
    for {
      iO <- queue.modify { q =>
        q.dequeueOption.fold((q, Option.empty[Int])) { // remove element to the queue
          case (i, q) => (q, Option(i))
        }
      }
      _ <- if (iO.nonEmpty) Console[F].println(s"Consumed item: ${iO.get}") else Sync[F].unit
      _ <- consumer(queue)
    } yield ()
  }

}