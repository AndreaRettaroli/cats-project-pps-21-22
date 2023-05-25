package api.movies.routes

import api.movies.Models.Director
import api.movies.MoviesStore
import api.movies.Utils.QueryParams.MovieIdQueryParamMatcher
import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.toMessageSyntax

import java.util.UUID

object DirectorRoutes {

  def routes[F[_]: Async](moviesStore: MoviesStore[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      /**
       * Get all directors in stored movies
       */
      case GET -> Root / "api" / "directors" =>
        moviesStore.getAllMoviesDirectors.flatMap {
          case directors if directors.nonEmpty => Ok(directors.asJson)
          case _                               => NoContent()
        }

      /**
       * Update the director on a specific movie
       */
      case req @ PUT -> Root / "api" / "director" :? MovieIdQueryParamMatcher(movieId) =>
        moviesStore.getMovieById(UUID.fromString(movieId)).flatMap {
          case Some(_) =>
            req
              .decodeJson[Director]
              .flatMap(newDirector => moviesStore.updateDirectorInAMovie(movieId, newDirector))
              .flatMap(_ => Ok("Updated successfully"))
          case _ => NotFound(s"Movie with id $movieId not found")
        }
    }
  }
}
