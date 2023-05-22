package api.movies.routes

import api.movies.MoviesStore
import org.http4s.HttpRoutes
import cats.syntax.all.*
import cats.effect.*
import org.http4s.dsl.io.*
import org.http4s.dsl.Http4sDsl
import com.comcast.ip4s.*
import cats.effect.IO
import io.circe.syntax._
import io.circe._
import org.http4s.circe.CirceEntityEncoder._
import io.circe.generic.auto._
import io.circe.syntax._



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
    }

  }
}
