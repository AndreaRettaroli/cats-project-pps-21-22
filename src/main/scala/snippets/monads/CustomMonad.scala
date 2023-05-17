package snippets.monads

import cats.Monad
import cats.syntax.all.*

import scala.annotation.tailrec

case class CustomMonad[A](value: A) {
  def flatMap[B](f: A => CustomMonad[B]): CustomMonad[B] =
    f(value)

  def map[B](f: A => B): CustomMonad[B] =
    CustomMonad(f(value))
}

object CustomMonad {
  implicit val customMonadInstance: Monad[CustomMonad] =
    new Monad[CustomMonad] {
      override def pure[A](x: A): CustomMonad[A] = CustomMonad(x)

      override def flatMap[A, B](fa: CustomMonad[A])(
          f: A => CustomMonad[B]
      ): CustomMonad[B] =
        fa.flatMap(f)

      @tailrec
      override def tailRecM[A, B](
          a: A
      )(f: A => CustomMonad[Either[A, B]]): CustomMonad[B] =
        f(a) match {
          case CustomMonad(either) =>
            either match {
              case Left(a)  => tailRecM(a)(f)
              case Right(b) => CustomMonad(b)
            }
        }
    }

}
