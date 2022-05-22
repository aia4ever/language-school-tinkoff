package teacher

import cats.effect.IO
import data.dto.{Lesson, Teacher}
import data.req.{BioReq, LessonUpdateReq, WithdrawalReq}
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import util.Util.{auth, rest}


object TeacherRouter {

  private implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  private implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]


  def teacherRouter(teacherService: TeacherService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "new_lesson" => req.decodeJson[Lesson.Insert]
        .flatMap(insert => auth(req).flatMap(teacherService.createLesson(_,insert)))
        .forbidden

      case req@POST -> Root / "bio-update" => req.decodeJson[BioReq]
        .flatMap(insert => auth(req).flatMap(teacherService.bioUpdate(_, insert)))
        .forbidden

      case req@GET -> Root / "upcoming-lessons" => auth(req).flatMap(teacherService.upcoming)
        .forbidden

      case req@GET -> Root / "next-lesson" => auth(req).flatMap(teacherService.next)
        .forbidden

      case req@GET -> Root / "previous-lessons" => auth(req).flatMap(teacherService.previous)
        .forbidden

      case req@GET -> Root / "lesson" / LongVar(lessonId) => auth(req)
        .flatMap(teacherService.getYourLesson(_, lessonId))
        .forbidden

      case req@POST -> Root / "lesson" => req.decodeJson[LessonUpdateReq]
        .flatMap(insert => auth(req).flatMap(teacherService.updateLesson(_,insert)))
        .forbidden

      case req@DELETE -> Root / "lesson" / LongVar(lessonId) => auth(req).flatMap(teacherService.delete(_, lessonId))
        .forbidden

      case req@POST -> Root / "withdrawal" => req.decodeJson[WithdrawalReq]
        .flatMap(insert => auth(req).flatMap(teacherService.withdrawal(_, insert)))
        .forbidden

      case req@POST -> Root / "payment" / LongVar(lessonId) => auth(req).flatMap(teacherService.payment(_, lessonId))
        .forbidden
    }
  }
}