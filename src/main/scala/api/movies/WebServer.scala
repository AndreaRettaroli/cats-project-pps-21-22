package api.movies

import api.movies.routes.MovieRoutes
import cats.syntax.all.*
import cats.effect.*
import org.http4s.dsl.io.*
import org.http4s.{EntityEncoder, HttpApp, HttpRoutes}
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Router
import org.typelevel.log4cats.noop.NoOpLogger

import scala.concurrent.duration.*


object Main extends IOApp {

  def createServer(app: HttpApp[IO]): IO[ExitCode] =
      EmberServerBuilder
        .default[IO]
        .withHttp2
        .withHost(ipv4"0.0.0.0")
        .withPort(port"9090")
        .withHttpApp(app)
        .withLogger(NoOpLogger[IO])
        .build
        .useForever
        .as(ExitCode.Success)


    def buildHttpApp[F[_] : Async](moviesStore: MoviesStore[F]): HttpApp[F] = {
        (MovieRoutes.routes(moviesStore)).orNotFound
    }

    override def run(args: List[String]): IO[ExitCode] = for {
      moviesStore <- MoviesStore.createWithSeedData[IO]
      httpApp = buildHttpApp(moviesStore)
      exitCode <- createServer(httpApp)
    } yield exitCode
}

