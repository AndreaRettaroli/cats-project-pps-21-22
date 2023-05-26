package snippets.fibers

import cats.effect.{ IO, IOApp }

object FiberExample1 extends IOApp.Simple {

  override val run: IO[Unit] = for {
    _ <- IO.println("Hello")
    _ <- IO.println("World")
  } yield ()
}

import cats.effect._

object FiberExample2 extends IOApp.Simple {

  override def run: IO[Unit] =
    IO.println("Hello") flatMap { _ =>
      IO.println("World")
    }
}

object FiberExample3 extends IOApp.Simple {

  override def run: IO[Unit] =
    IO.println("Hello") >> IO.println("World")
}

object FiberExample4 extends IOApp.Simple {

  override def run: IO[Unit] =
    IO.println("Hello") *> IO.println("World")
}
