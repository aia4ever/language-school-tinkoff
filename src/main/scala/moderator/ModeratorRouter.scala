package moderator

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import util.Util.rest

object ModeratorRouter {
  def moderatorRouter(moderatorService: ModeratorService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "block" / LongVar(userId) =>
        moderatorService.blockUser(req, userId).forbidden

      case req@POST -> Root / "unblock" / LongVar(userId) => moderatorService.unblock(req,userId)
        .forbidden

      case req@DELETE -> Root / "delete" / "user" / LongVar(userId) => moderatorService.deleteUser(req, userId)
        .forbidden

      case req@DELETE -> Root / "delete" / "lesson" / LongVar(lessonId) => moderatorService.deleteLesson(req, lessonId)
        .forbidden

      case req@GET -> Root / "lesson" / LongVar(lessonId) => moderatorService.getLesson(req, lessonId).forbidden

      case req@GET -> Root / "user" / LongVar(userId) => moderatorService.getUser(req, userId)
      .forbidden
    }
  }
}
