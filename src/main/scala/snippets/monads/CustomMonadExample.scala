package snippets.monads

import cats.implicits._

object CustomMonadExample extends App {
  for {
    a <- CustomMonad(1)
    b <- CustomMonad(2).flatMap(x => CustomMonad(x + 1))
  } yield print(a + b)
}
