package api.movies.routes

import api.movies.Models.{ Movie, MovieWithId }
import api.movies.MoviesStore
import org.http4s.{ HttpRoutes, Response }
import cats.syntax.all.*
import cats.effect.*
import org.http4s.dsl.io.*
import org.http4s.dsl.Http4sDsl
import com.comcast.ip4s.*
import cats.effect.IO
import io.circe.syntax.*
import io.circe.generic.auto.*
import io.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.toMessageSyntax
import api.movies.Utils.QueryParams.*
import cats.data.Validated.{ Invalid, Valid }
import org.http4s.client.{ Client, JavaNetClientBuilder }
import cats.implicits._
import cats.effect.implicits._

import scala.concurrent.duration.*

object MovieRoutes {

  def routes[F[_]: Async](moviesStore: MoviesStore[F]): HttpRoutes[F] = {
    val client: Client[F] = JavaNetClientBuilder[F].create
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok("Hello World!")

      /**
       * Get movie by ID
       */
      case GET -> Root / "api" / "movie" / UUIDVar(movieId) =>
        moviesStore
          .getMovieById(movieId)
          .flatMap(maybeMovie =>
            maybeMovie.fold(NotFound(s"Wrong id $movieId"))(movie => Ok(movie.asJson))
          )

      /**
       * Create movie
       */
      case req @ POST -> Root / "api" / "movie" =>
        for {
          movie <- req.decodeJson[Movie]
          addedMovie <- moviesStore.createMovie(movie)
          response <- Created(addedMovie.asJson)
        } yield response

      /**
       * Update Movie
       */
      case req @ PUT -> Root / "api" / "movie" / UUIDVar(movieId) =>
        for {
          maybeMovie <- moviesStore.getMovieById(movieId)
          movieToAdd <- req.decodeJson[Movie]
          response <- maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie =>
            moviesStore.updateMovie(movieId, movieToAdd) >> Ok(
              new MovieWithId(movieId.toString, movieToAdd).asJson
            )
          )
        } yield response

      /**
       * Delete movie
       */
      case DELETE -> Root / "api" / "movie" / UUIDVar(movieId) =>
        for {
          maybeMovie <- moviesStore.getMovieById(movieId)
          response <- maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie =>
            moviesStore.deleteMovie(movieId) >> Ok(s"Successfully deleted movie ${movie.id}")
          )
        } yield response

      /**
       * Get movies list directed by director, can also filter them per year
       */
      case GET -> Root / "api" / "movies" :? DirectorQueryParamMatcher(
            director
          ) +& OptionalYearQueryParamMatcher(maybeYear) =>
        moviesStore
          .getFilteredMoviesByDirector(director)
          .flatMap(movies =>
            if (movies.isEmpty) NotFound(s"No results found for: $director")
            else
              maybeYear.fold(Ok(movies.asJson)) {
                case Invalid(_) => BadRequest("Invalid year passed")
                case Valid(year) =>
                  val moviesInYear = movies.filter(_.movie.year === year.getValue)
                  if (moviesInYear.nonEmpty) Ok(moviesInYear.asJson)
                  else NotFound(s"No movies directed for ${director} in ${year}")
              }
          )

      /**
       * Get movies list by year
       */
      case GET -> Root / "api" / "movies" :? YearQueryParamMatcher(maybeYear) =>
        def findMoviesBy(year: Int): F[Response[F]] = for {
          movies <- moviesStore.getFilteredMoviesByYear(year)
          response <-
            if (movies.nonEmpty) Ok(movies.asJson) else NotFound(s"No movies in ${year} found")
        } yield response

        maybeYear.fold(_ => BadRequest("Invalid year passed"), year => findMoviesBy(year.getValue))

      /**
       * Get movies list by genre
       */
      case GET -> Root / "api" / "movies" :? GenreQueryParamMatcher(genre) =>
        moviesStore
          .getFilteredMoviesByGenre(genre)
          .flatMap(movies =>
            if (movies.isEmpty) NotFound(s"No movie found with genre: $genre")
            else Ok(movies.asJson)
          )

      /**
       * Get movies list by actor
       */
      case GET -> Root / "api" / "movies" :? ActorQueryParamMatcher(actor) =>
        moviesStore
          .getFilteredMoviesByActor(actor)
          .flatMap(movies =>
            if (movies.isEmpty) NotFound(s"No movie found with actor: $actor")
            else Ok(movies.asJson)
          )

      /**
       * Get movies list
       */
      case GET -> Root / "api" / "movies" =>
        moviesStore.getAllMovies.flatMap(movies =>
          if (movies.nonEmpty) Ok(movies.asJson) else NoContent()
        )

      /**
       * Get movies rating
       */
      case GET -> Root / "api" / "movies" / "rating" =>
        for {
          movies <- moviesStore.getAllMovies
          titles = movies.map(_.movie.title)
          movieRatingPairsList <- titles.parTraverse(title =>
            moviesStore.getMoviesRating(title, client).map(r => (title, r))
          )
          (bestTitle, score) = movieRatingPairsList.maxBy(_._2)
          response <- Ok(
            s"${movieRatingPairsList.map(t => s"${t._1}: ${t._2}").mkString("; ")}. The best movie is $bestTitle with a score of $score"
          )
        } yield response

    }
  }
}
