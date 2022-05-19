package moderator

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.io._
import util.Util.{auth, rest}

object ModeratorRouter {
  def moderatorRouter(moderatorService: ModeratorService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "block" / LongVar(userId) =>auth(req)
        .flatMap(moderatorService.blockUser(_, userId))
        .forbidden

      case req@POST -> Root / "unblock" / LongVar(userId) => auth(req)
        .flatMap(moderatorService.unblock(_,userId))
        .forbidden

      case req@DELETE -> Root / "delete" / "user" / LongVar(userId) => auth(req)
        .flatMap(moderatorService.deleteUser(_, userId))
        .forbidden

      case req@DELETE -> Root / "delete" / "lesson" / LongVar(lessonId) => auth(req)
        .flatMap(moderatorService.deleteLesson(_, lessonId))
        .forbidden

      case req@GET -> Root / "lesson" / LongVar(lessonId) => auth(req)
        .flatMap(moderatorService.getLesson(_, lessonId))
        .forbidden

      case req@GET -> Root / "user" / LongVar(userId) => auth(req)
        .flatMap(moderatorService.getUser(_, userId))
      .forbidden
    }
  }
}
