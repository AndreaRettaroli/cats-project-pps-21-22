package snippets.semigroup

import cats.Semigroup

object SemigroupExample extends App {
  implicit val multiplicationSemigroup: Semigroup[Int] = (x: Int, y: Int) =>
    x * y

  print(Semigroup[Int].combine(3, 3))
}
