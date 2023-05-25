package api.movies.routes

import cats.effect.Async
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import api.movies.MoviesStore
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

object ActorRoutes {

  def routes[F[_]: Async](moviesStore: MoviesStore[F]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._

    HttpRoutes.of[F] {
      /**
       * Get actors list
       */
      case GET -> Root / "api" / "actors" =>
        moviesStore.getAllMoviesActors.flatMap {
          case actors if actors.nonEmpty => Ok(actors.asJson)
          case _                         => NoContent()
        }
    }
  }
}
