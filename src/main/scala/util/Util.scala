package util

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.{Request, Response}



object Util {

  def auth(req: Request[IO]): IO[String] = req.headers.get(Authorization.name) match {
    case Some(s) => s.head.value.pure[IO]
    case None => IO.raiseError(new Exception("no session"))
  }
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