package moderator

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import session.SessionService
import util.Util.{auth, rest}

object ModeratorRouter {
  def moderatorRouter(moderatorService: ModeratorService, sessionService: SessionService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "block" / LongVar(userId) =>
        (for {
         session <- auth(req)
         id <- sessionService.checkSession(session)
         is <- moderatorService.isMod(id)
         isReqMod <- moderatorService.isMod(userId)
         res <- if (is && isReqMod) moderatorService.blockUser(userId)
         else IO.raiseError(new Exception("Something went wrong"))
        } yield res).forbidden

      case req@POST -> Root / "unblock" / LongVar(userId) =>
        (for {
          session <- auth(req)
          id <- sessionService.checkSession(session)
          is <- moderatorService.isMod(id)
          res <- if (is) moderatorService.unblockUser(userId)
          else IO.raiseError(new Exception("Something went wrong"))
        } yield res).forbidden

      case req@DELETE -> Root / "delete" / "user" / LongVar(userId) =>
        (for {
          session <- auth(req)
          id <- sessionService.checkSession(session)
          is <- moderatorService.isMod(id)
          isReqMod <- moderatorService.isMod(userId)
          res <- if (is && isReqMod) moderatorService.deleteUser(userId)
          else IO.raiseError(new Exception("Something went wrong"))
        } yield res
        ).forbidden

      case req@DELETE -> Root / "delete" / "lesson" / LongVar(lessonId) =>
        (for {
          session <- auth(req)
          id <- sessionService.checkSession(session)
          is <- moderatorService.isMod(id)
          res <- if (is) moderatorService.deleteUser(lessonId)
          else IO.raiseError(new Exception("Something went wrong"))
        } yield res
          ).forbidden

      case req@GET -> Root / "lesson" / LongVar(lessonId) =>
        ( for {
          session <- auth(req)
          id <- sessionService.checkSession(session)
          is <- moderatorService.isMod(id)
          res <- if (is) moderatorService.lessonById(lessonId)
          else IO.raiseError(new Exception("Something went wrong"))
        } yield res ).forbidden

      case req@GET -> Root / "lesson" / LongVar(userId) =>
        ( for {
          session <- auth(req)
          id <- sessionService.checkSession(session)
          is <- moderatorService.isMod(id)
          res <- if (is) moderatorService.userById(userId)
          else IO.raiseError(new Exception("Something went wrong"))
        } yield res ).forbidden
    }
  }
}
