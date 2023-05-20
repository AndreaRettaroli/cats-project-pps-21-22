package useCase.file.simple

import useCase.file.PolymorphicUtils.copy
import cats.effect.{ExitCode, IO, IOApp}

import java.io.File

/**
 * The goal of this program is copies files using Cats-Effect and Functional Programming.
 */

object PolymorphicCopyFile extends IOApp{
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- if (args.length != 2) IO.raiseError(new IllegalArgumentException("Add origin and destination files as args"))
      else IO.unit
      originFile = new File(args.head)
      destinationFile = new File(args.tail.head)
      count <- copy[IO](originFile, destinationFile)
      _ <- IO.println(s"$count bytes copied from ${originFile.getPath} to ${destinationFile.getPath}")
    } yield ExitCode.Success
  }
}
