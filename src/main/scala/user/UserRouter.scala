package user

import cats.effect.IO
import data.dto.User
import data.req.LoginReq
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import session.SessionService
import util.Util.{auth, rest}


object UserRouter {


  implicit val userEncoder: EntityEncoder[IO, User] = jsonEncoderOf[IO, User]

  def userRouter(userService: UserService, sessionService: SessionService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "create"  => req.decodeJson[User.Insert].flatMap(userService.create)
        .flatMap(res => Created(res.asJson))
        .handleErrorWith(_ => Forbidden("User with this login/phone-number/email already exists"))

      case req@POST -> Root / "login" => req.decodeJson[LoginReq].flatMap(userService.login)
        .forbidden

      case req@GET -> Root / "logout"  => (for {
        session <- auth(req)
        res <- userService.logout(session)
      } yield res).forbidden

      case req@DELETE -> Root /  "delete" / LongVar(id)  => (for {
        session <- auth(req)
        res <- userService.deleteUser(id, session)} yield res)
        .flatMap(_ => Ok(s"User with id:$id deleted"))
        .handleErrorWith(err => Forbidden(err.getMessage.asJson))

      case req@GET -> Root / "profile" => auth(req).flatMap(sessionService.checkSession).flatMap(userService.byId)
        .forbidden
    }
  }
}
