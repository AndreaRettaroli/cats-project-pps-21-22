package snippets.monads

import cats.implicits._

object CustomMonadExample extends App {
  val res = for {
    a <- CustomMonad(1)
    b <- CustomMonad(2).flatMap(x => CustomMonad(x + 1))
  } yield a + b
  println(res)
}
