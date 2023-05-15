package snippets

import cats.effect.{IOApp,IO}

object HelloWorld extends IOApp.Simple {
  val run: IO[Unit] = IO.println("Hello, World!")
}
