package util

import cats.effect.IO
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.Response
import org.http4s.circe._
import org.http4s.dsl.io._



object Util {
  implicit class rest[T](io: IO[T]) {

    def forbidden(implicit enc: Encoder[T]): IO[Response[IO]] =
      io
        .flatMap(res => Ok(res.asJson))
        .handleErrorWith(err =>
          Forbidden(err.getMessage.asJson))

    def notFound(implicit enc: Encoder[T]): IO[Response[IO]] =
      io
        .flatMap(res => Ok(res.asJson))
        .handleErrorWith(err =>
          NotFound(err.getMessage.asJson))

  }
}