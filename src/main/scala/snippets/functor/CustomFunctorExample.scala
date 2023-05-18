package snippets.functor

import cats.Functor
import cats.syntax.functor._

object CustomFunctorExample extends App{
  case class CustomFunctor[A](value: A)

  object CustomFunctor {
    implicit val functor: Functor[CustomFunctor] = new Functor[CustomFunctor] {
      def map[A, B](fa: CustomFunctor[A])(f: A => B): CustomFunctor[B] =
        CustomFunctor(f(fa.value))
    }
  }

  print(CustomFunctor(5).map(_ + 1))
}




