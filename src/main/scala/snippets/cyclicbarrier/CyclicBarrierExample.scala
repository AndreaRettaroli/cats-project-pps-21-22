package snippets.cyclicbarrier

import cats.implicits._
import cats.effect.{ ExitCode, IO, IOApp }
import cats.effect.std.CyclicBarrier
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration._

object CyclicBarrierExample extends IOApp.Simple {

  override def run: IO[Unit] =
    for {
      barrier <- CyclicBarrier[IO](2)
      f1 <- (IO.println("fast fiber before barrier") >>
        barrier.await >>
        IO.println("fast fiber after barrier")).start
      f2 <- (IO.sleep(1.second) >>
        IO.println("slow fiber before barrier") >>
        IO.sleep(1.second) >>
        barrier.await >>
        IO.println("slow fiber after barrier")).start
      _ <- (f1.join, f2.join).tupled
    } yield ExitCode.Success
}
