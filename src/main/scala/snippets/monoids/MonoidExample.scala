package snippets.monoids

import cats.Monoid

object MonoidExample extends App {

  implicit val intMonoid: Monoid[Int] = new Monoid[Int] {
    override def empty: Int = 0

    override def combine(x: Int, y: Int): Int = x + y
  }

  implicit val stringMonoid: Monoid[String] = new Monoid[String] {
    override def empty: String = ""

    override def combine(x: String, y: String): String = x + y
  }

  def sum[A: Monoid](as: List[A]): A =
    as.foldLeft(Monoid[A].empty)(Monoid[A].combine)

  println(sum(List(1, 2, 3, 4, 5)))
  println(sum(List("hello", " ", "world")))
  println(sum(List(Set(1, 2), Set(2, 3, 4, 5))))
}
