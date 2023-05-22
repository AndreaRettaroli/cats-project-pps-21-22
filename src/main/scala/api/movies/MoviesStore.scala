package api.movies

import cats.effect.kernel.Ref
import cats.effect.{Async, Sync}
import cats.implicits._
import Models._

class MoviesStore[F[_] : Async](private val stateRef: Ref[F, MoviesStore.State]) {
  /* Movies */
  def getAllMovies: F[List[MovieWithId]] = stateRef.get
}

object MoviesStore {
  type State = List[MovieWithId]

  def apply[F[_] : Async](stateRef: Ref[F, State]): MoviesStore[F] = new MoviesStore[F](stateRef)

  def empty[F[_] : Async]: F[MoviesStore[F]] = Ref.of[F, State](Nil).map(MoviesStore[F])

  private val seedState: State = List(
    MovieWithId("9127c44c-7c72-44a8-8bcb-088e3b659eca", Movie(
      "Titanic",
      1997,
      List(Actor("Kate", "Winslet", 57), Actor("Leonardo", "DiCaprio", 44), Actor("Billy", "Zane", 63)),
      Director("James", "Cameron", "Canadian", 44),
      List("Romance", "Drama", "Epic", "Disaster"),
      2_195_170_204L,
      11
    )
    ),
    MovieWithId("af9ce051-8541-42a9-88c4-e36d5036ad1e", Movie(
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
