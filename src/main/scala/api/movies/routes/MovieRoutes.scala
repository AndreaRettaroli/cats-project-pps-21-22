package api.movies.routes

import api.movies.Models.{Movie, MovieWithId}
import api.movies.MoviesStore
import org.http4s.{HttpRoutes, Response}
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




import scala.concurrent.duration.*


object MovieRoutes {
  def routes[F[_] : Async](moviesStore: MoviesStore[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

     HttpRoutes.of[F] {
      case GET -> Root  =>
        Ok("Hello World!")

        /**
         * Get movies list
         */
      case GET -> Root / "api" / "movies" =>
        moviesStore.getAllMovies.flatMap(movies =>
          if (movies.nonEmpty) Ok(movies.asJson) else NoContent()
        )

        /**
         * Get movie by ID
         */
      case GET -> Root /  "api" / "movie" / UUIDVar(movieId) =>
        moviesStore.getMovieById(movieId).flatMap(maybeMovie =>
          maybeMovie.fold(NotFound(s"Wrong id $movieId"))(movie => Ok(movie.asJson)
          )
        )

      /**
       * Create movie
       */
      case req@POST -> Root /  "api" / "movie" =>
        for {
          movie <- req.decodeJson[Movie]
          addedMovie <- moviesStore.createMovie(movie)
          response <- Created(addedMovie.asJson)
        } yield response

      /**
       * Update Movie
       */
      case req@PUT -> Root /  "api" / "movie" / UUIDVar(movieId) =>
        for {
          maybeMovie <- moviesStore.getMovieById(movieId)
          movieToAdd <- req.decodeJson[Movie]
          response <- maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie =>
            moviesStore.updateMovie(movieId, movieToAdd) >> Ok(new MovieWithId(movieId.toString,movieToAdd).asJson)
          )
        } yield response

      /**
       * Delete movie
       */
      case DELETE -> Root /  "api" / "movie" / UUIDVar(movieId) =>
        for {
          maybeMovie <- moviesStore.getMovieById(movieId)
          response <- maybeMovie.fold(NotFound(s"No movie with id $movieId found"))(movie =>
            moviesStore.deleteMovie(movieId) >> Ok(s"Successfully deleted movie ${movie.id}")
          )
        } yield response
     }

  }
}
