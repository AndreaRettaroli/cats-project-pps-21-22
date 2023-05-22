package api.movies

import api.movies.Models.{Actor, Director, Movie}
import api.movies.Main.buildHttpApp
import org.http4s.client.UnexpectedStatus
import cats.effect.IO
import io.circe.Json
import org.http4s.Status.{NotFound, Ok}
import org.http4s.{Method, Request, Response, Uri}
import org.http4s.client.Client
import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityCodec._

class TestMoviesAPIs extends CatsEffectSuite {
  private var client: Option[Client[IO]] = None

  private def getUriFromPath(path: String): Uri =
    Uri.fromString(s"http://localhost:9090/api/$path").toOption.get
  override def beforeAll(): Unit = {
    val moviesRepository = MoviesStore.createWithSeedData[IO].unsafeRunSync()
    client = Some(Client.fromHttpApp(buildHttpApp[IO](moviesRepository)))
  }

  test("Wrong route") {
    val request: Request[IO] = Request(method = Method.GET, uri = getUriFromPath("wrong-path"))
    for {
      response <- client.get.expect[Json](request).attempt
      _ <- assertIO(IO(response.isLeft && response.left.exists(_.isInstanceOf[org.http4s.client.UnexpectedStatus])), true)
    } yield ()
  }


  /* Movies */
  test("Get all movies") {
    val request: Request[IO] = Request(method = Method.GET, uri = getUriFromPath("movies"))
    for {
      json <- client.get.expect[Json](request)
      _ <- IO.println(json.asArray)
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 2)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

}
