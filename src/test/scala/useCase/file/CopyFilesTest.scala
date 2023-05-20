package useCase.file


import useCase.file.Utils.{copy, getAmountOfBytesFromFile}
import munit.CatsEffectSuite

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files}
import java.util.concurrent.TimeUnit

class CopyFilesTest extends CatsEffectSuite {
  var sourceFile: Option[File] = None
  var destinationFile: Option[File] = None

  override def beforeAll(): Unit = {
    sourceFile = Some(Files.createTempFile("tmp", ".txt").toFile)
    destinationFile = Some(Files.createTempFile("tmp", ".txt").toFile)
    Files.write(sourceFile.get.toPath, "Hello World".getBytes(StandardCharsets.UTF_8))
  }

  override def afterAll(): Unit = {
    print(sourceFile.get.toPath)
    print(destinationFile.get.toPath)
    Files.deleteIfExists(sourceFile.get.toPath)
    Files.deleteIfExists(destinationFile.get.toPath)

  }


  test("Verify that destination file is empty") {
    assertIO(getAmountOfBytesFromFile(destinationFile.get), -1L)
  }

  test("Verify that origin file is not empty") {
    getAmountOfBytesFromFile(sourceFile.get).map(bytes => assert(bytes > 0))
  }

  test("Verify copy from source to destination") {
    for {
      bytesTransferred <- copy(sourceFile.get, destinationFile.get)
      bytesDestinationFile <- getAmountOfBytesFromFile(destinationFile.get)
      _ <- assertIO(getAmountOfBytesFromFile(sourceFile.get), bytesDestinationFile)
      _ <- assertIO(getAmountOfBytesFromFile(destinationFile.get), bytesTransferred)
    } yield ()
  }
}