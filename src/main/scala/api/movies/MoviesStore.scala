package api.movies

import cats.effect.kernel.Ref
import cats.effect.{Async, Sync}
import cats.implicits._
import Models._
import java.util.UUID

class MoviesStore[F[_] : Async](private val stateRef: Ref[F, MoviesStore.State]) {
  /* Movies crud */
  def getAllMovies: F[List[MovieWithId]] = stateRef.get
  def getMovieById(id: UUID): F[Option[MovieWithId]] = stateRef.get.map(_.find(_.id == id.toString))
  def createMovie(movie: Movie): F[MovieWithId] = for {
    uuid <- Sync[F].delay(UUID.randomUUID().toString)
    movieToAdd = MovieWithId(uuid, movie)
    _ <- stateRef.update(state => (movieToAdd :: state))
  } yield movieToAdd
  def updateMovie(id: UUID, movie: Movie): F[Unit] = stateRef.update(state =>
    state.find(_.id == id.toString) match {
      case Some(_) => MovieWithId(id.toString, movie) :: state.filterNot(_.id == id.toString)
      case None => state
    }
  )
  def deleteMovie(id: UUID): F[Unit] = stateRef.update(state =>
    state.filterNot(_.id == id.toString)
  )


}

object MoviesStore {
  type State = List[MovieWithId]

  def apply[F[_] : Async](stateRef: Ref[F, State]): MoviesStore[F] = new MoviesStore[F](stateRef)

  def empty[F[_] : Async]: F[MoviesStore[F]] = Ref.of[F, State](Nil).map(MoviesStore[F])

  private val seedState: State = List(
    MovieWithId("e73a99e4-2554-4d29-bd94-651b282e81ab", Movie(
      "Titanic",
      1997,
      List(Actor("Kate", "Winslet", 57), Actor("Leonardo", "DiCaprio", 44), Actor("Billy", "Zane", 63)),
      Director("James", "Cameron", "Canadian", 44),
      List("Romance", "Drama", "Epic", "Disaster"),
      2_195_170_204L,
      11
    )
    ),
    MovieWithId("957675e9-5480-426f-83fb-4c1f0c7a060e", Movie(
      "Top Gun",
      1986,
      List(Actor("Tom", "Cruise", 73), Actor("Kelly", "McGillis", 10), Actor("Val", "Kilmer", 25)),
      Director("Tony", "Scott", "British", 55),
      List("Action", "Romance", "Drama", "Adventure"),
      356_800_000L,
      0
    )
    )
  )

  def createWithSeedData[F[_] : Async]: F[MoviesStore[F]] = Ref.of[F, State](seedState).map(MoviesStore[F])
}
