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
import io.circe.syntax.*
import io.circe.generic.auto.*
import io.circe.*

class TestMoviesAPIs extends CatsEffectSuite {
  private val TITANIC_ID = "e73a99e4-2554-4d29-bd94-651b282e81ab"

  private val newMovie: Movie = Movie(
    "The Terminator",
    1984,
    List(Actor("Arnold", "Schwarzenegger", 10)),
    Director("James", "Cameron", "Canadian", 44),
    List("Action", "Horror", "Thriller", "Fantasy"),
    70_000_000,
    3
  )

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
      _ <- assertIO(IO(json.asArray.fold(0)(_.size)), 2)
      _ <- assertIO(IO(json.asArray.get.head.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Get movie by id") {
    val request: Request[IO] = Request(method = Method.GET, uri = getUriFromPath(s"movie/$TITANIC_ID"))
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.hcursor.downField("movie").get[String]("title").toOption), Some("Titanic"))
    } yield ()
  }

  test("Create a new movie") {
    val request: Request[IO] = Request(method = Method.POST, uri = getUriFromPath("movie")).withEntity(newMovie.asJson)
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.hcursor.downField("movie").get[String]("title").toOption), Some(newMovie.title))
    } yield ()
  }

  test("Update a movie") {
    val request: Request[IO] = Request(method = Method.PUT, uri = getUriFromPath(s"movie/$TITANIC_ID"))
      .withEntity(newMovie.asJson)
    for {
      json <- client.get.expect[Json](request)
      _ <- assertIO(IO(json.hcursor.downField("movie").get[String]("title").toOption), Some(newMovie.title))
    } yield ()
  }

  test("Delete a movie") {
    val requestAllMoviesBefore: Request[IO] = Request(method = Method.GET, uri = getUriFromPath("movies"))
    val request: Request[IO] = Request(method = Method.DELETE, uri = getUriFromPath(s"movie/$TITANIC_ID"))
    val requestAllMoviesAfter: Request[IO] = Request(method = Method.GET, uri = getUriFromPath("movies"))
    for {
      jsonAllMoviesBefore <- client.get.expect[Json](requestAllMoviesBefore)
      moviesStoreSize <- IO(jsonAllMoviesBefore.asArray.fold(0)(_.size))
      json <- client.get.expect[Json](request)
      _ <- IO(json.asString.get).map(s => assert(s == "Successfully deleted movie " +
        TITANIC_ID))
      jsonAllMovies <- client.get.expect[Json](requestAllMoviesAfter)
      _ <- assertIO(IO(jsonAllMovies.asArray.fold(0)(_.size)), moviesStoreSize - 1 )
    } yield ()
  }

}
