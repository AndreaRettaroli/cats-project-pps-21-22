package snippets.semigroup

import cats.Semigroup
import cats.syntax.all._

object SemigroupUseExample extends App {
  private val one: Option[Int] = Option(1)
  private val two: Option[Int] = Option(2)

  println(Semigroup[Int].combine(1, 2))
  println(Semigroup[List[Int]].combine(List(1, 2, 3), List(4, 5, 6)))
  println(Semigroup[Option[Int]].combine(one, two))
  println(Semigroup[Option[Int]].combine(Option(1), None))
  println(Semigroup[Int => Int].combine(_ + 1, _ * 10).apply(6))
  println(one |+| two) // |+| is the semigroup operator

}
