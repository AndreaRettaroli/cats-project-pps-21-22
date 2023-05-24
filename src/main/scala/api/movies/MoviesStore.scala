package api.movies

import cats.effect.kernel.Ref
import cats.effect.{ Async, IO, Sync }
import cats.implicits.*
import Models.*
import api.movies.Utils.IMDB
import org.http4s.EntityDecoder
import org.http4s.circe.*
import org.http4s.client.Client
import io.circe.Json
import org.http4s.circe.jsonOf

import java.util.UUID

class MoviesStore[F[_]: Async](private val stateRef: Ref[F, MoviesStore.State]) {
  /* Movies crud */
  def getMovieById(id: UUID): F[Option[MovieWithId]] = stateRef.get.map(_.find(_.id == id.toString))

  def createMovie(movie: Movie): F[MovieWithId] = for {
    uuid <- Sync[F].delay(UUID.randomUUID().toString)
    movieToAdd = MovieWithId(uuid, movie)
    _ <- stateRef.update(state => (movieToAdd :: state))
  } yield movieToAdd

  def updateMovie(id: UUID, movie: Movie): F[Unit] = stateRef.update(state =>
    state.find(_.id == id.toString) match {
      case Some(_) => MovieWithId(id.toString, movie) :: state.filterNot(_.id == id.toString)
      case None    => state
    }
  )

  def deleteMovie(id: UUID): F[Unit] =
    stateRef.update(state => state.filterNot(_.id == id.toString))
  /*get movies list with filters*/
  def getAllMovies: F[List[MovieWithId]] = stateRef.get

  def getFilteredMoviesByYear(year: Int): F[List[MovieWithId]] =
    stateRef.get.map(_.filter(_.movie.year == year))

  def getFilteredMoviesByGenre(genre: String): F[List[MovieWithId]] =
    stateRef.get.map(_.filter(_.movie.genres.contains(genre)))

  def getFilteredMoviesByActor(actor: String): F[List[MovieWithId]] = stateRef.get.map(state =>
    actor.split(" ") match {
      case Array(name, surname) =>
        state.filter(movieWithId =>
          movieWithId.movie.actors
            .map(actor => (actor.firstName, actor.lastName))
            .contains((name, surname))
        )
      case _ => Nil
    }
  )

  def getFilteredMoviesByDirector(director: String): F[List[MovieWithId]] =
    stateRef.get.map(state =>
      director.split(" ") match {
        case Array(name, surname) =>
          state.filter(movieWithId =>
            movieWithId.movie.director.firstName == name
              && movieWithId.movie.director.lastName == surname
          )
        case _ => Nil
      }
    )

  def getMoviesRating(title: String, client: Client[F])(implicit
      jsonDecoder: EntityDecoder[F, Json] = jsonOf[F, Json]): F[Double] = {
    val informationMovieUrl = IMDB.getIMDBMovieInfoUrl(title.replaceAll(" ", "%20"))
    for {
      informationMovieJson <- client.expect[Json](informationMovieUrl)
      movieId = informationMovieJson.hcursor
        .downField("results")
        .downArray
        .get[String]("id")
        .toOption
        .get
      urlRating = IMDB.getIMDBRatingUrl(movieId)
      ratingMovieJson <- client.expect[Json](urlRating)
      rating = ratingMovieJson.hcursor.get[String]("imDb").toOption.get.toDouble
    } yield rating

  }

}

object MoviesStore {
  type State = List[MovieWithId]

  def apply[F[_]: Async](stateRef: Ref[F, State]): MoviesStore[F] = new MoviesStore[F](stateRef)

  def empty[F[_]: Async]: F[MoviesStore[F]] = Ref.of[F, State](Nil).map(MoviesStore[F])

  private val seedState: State = List(
    MovieWithId(
      "e73a99e4-2554-4d29-bd94-651b282e81ab",
      Movie(
        "Titanic",
        1997,
        List(
          Actor("Kate", "Winslet", 57),
          Actor("Leonardo", "DiCaprio", 44),
          Actor("Billy", "Zane", 63)
        ),
        Director("James", "Cameron", "Canadian", 44),
        List("Romance", "Drama", "Epic", "Disaster"),
        2_195_170_204L,
        11
      )
    ),
    MovieWithId(
      "957675e9-5480-426f-83fb-4c1f0c7a060e",
      Movie(
        "Top Gun",
        1986,
        List(
          Actor("Tom", "Cruise", 73),
          Actor("Kelly", "McGillis", 10),
          Actor("Val", "Kilmer", 25)
        ),
        Director("Tony", "Scott", "British", 55),
        List("Action", "Romance", "Drama", "Adventure"),
        356_800_000L,
        0
      )
    )
  )

  def createWithSeedData[F[_]: Async]: F[MoviesStore[F]] =
    Ref.of[F, State](seedState).map(MoviesStore[F])
}
