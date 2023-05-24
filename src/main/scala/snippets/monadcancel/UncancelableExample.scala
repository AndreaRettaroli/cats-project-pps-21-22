package snippets.monadcancel

import cats.effect.{ IO, IOApp }
import cats.effect.unsafe.implicits.global

object UncancelableExample extends App {

  def run =
    for {
      fib <- (IO.uncancelable(_ => IO.canceled >> IO.println("Hello"))
        >> IO.println(" World!")).start
      res <- fib.join
    } yield res // print hello and cancel execution

  println(run.unsafeRunSync())
}
