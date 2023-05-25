package api.movies

import org.http4s._
import org.http4s.dsl.impl.{
  OptionalValidatingQueryParamDecoderMatcher,
  QueryParamDecoderMatcher,
  ValidatingQueryParamDecoderMatcher
}

import java.time.Year

object Utils {

  object QueryParams {

    implicit val yearQueryParamDecoder: QueryParamDecoder[Year] =
      QueryParamDecoder[Int].map(year => Year.of(year))

    case object YearQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Year]("year")

    case object GenreQueryParamMatcher extends QueryParamDecoderMatcher[String]("genre")

    case object ActorQueryParamMatcher extends QueryParamDecoderMatcher[String]("actor")

    case object DirectorQueryParamMatcher extends QueryParamDecoderMatcher[String]("director")

    case object OptionalYearQueryParamMatcher
        extends OptionalValidatingQueryParamDecoderMatcher[Year]("year")
    case object MovieIdQueryParamMatcher extends QueryParamDecoderMatcher[String]("movieId")

  }

  object IMDB {

    private val apiKey: String = sys.env.getOrElse(
      "API_KEY", {
        println("Failed to retrieve API key from system environment variables.")
        sys.exit(1)
      }
    )

    def getIMDBMovieInfoUrl(title: String): String =
      s"https://imdb-api.com/en/API/Search/$apiKey/$title"

    def getIMDBRatingUrl(movieId: String): String =
      s"https://imdb-api.com/en/API/Ratings/$apiKey/$movieId"
  }
}
