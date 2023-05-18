package snippets.monoids

import cats.Monoid

object MultiplicationMonoidExample extends App {
  implicit val multiplicationMonoid: Monoid[Int] = new Monoid[Int] {
    override val empty: Int = 1

    override def combine(x: Int, y: Int): Int = x * y
  }

  println(Monoid[Int].combine(Monoid[Int].empty, 2))
  println(Monoid[Int].combine(1, 2))
}
