package snippets.typeclasses

class CatsTypeclassesOverview {

  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  trait Monoid[A] extends Semigroup[A] {
    def empty: A
  }

  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  trait Apply[F[_]] extends Functor[F] {
    def ap[A, B](ff: F[(A) => B])(fa: F[A]): F[B]

  }

  trait Applicative[F[_]] extends Apply[F] {
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
    def pure[A](a: A): F[A]
    def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)
  }

  trait FlatMap[F[_]] extends Functor[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  trait Monad[F[_]] extends Applicative[F] with FlatMap[F] {
    override def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(a => pure(f(a)))
    def flatten[A](ffa: F[F[A]]): F[A]
    def flatMap[A, B](fa: F[A])(f: (A) => F[B]): F[B]
    def pure[A](x: A): F[A]
    def tailRecM[A, B](a: A)(f: (A) => F[Either[A, B]]): F[B]
  }

  trait ApplicativeError[F[_], E] extends Applicative[F] {
    def raiseError[A](e: E): F[A]
    def handleErrorWith[A](fa: F[A])(f: E => F[A]): F[A]
    def handleError[A](fa: F[A])(f: E => A): F[A]
    def attempt[A](fa: F[A]): F[Either[E, A]]
    // More functions elided
  }

  trait MonadError[F[_], E] extends ApplicativeError[F, E] with Monad[F] {
    def ensure[A](fa: F[A])(error: => E)(predicate: A => Boolean): F[A]
    def ensureOr[A](fa: F[A])(error: A => E)(predicate: A => Boolean): F[A]
    def adaptError[A](fa: F[A])(pf: PartialFunction[E, E]): F[A]
    def rethrow[A, EE <: E](fa: F[Either[EE, A]]): F[A]
  }
}
