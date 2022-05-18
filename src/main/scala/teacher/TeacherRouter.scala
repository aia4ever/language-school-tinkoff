package teacher

import cats.effect.IO
import data.dto.{Lesson, Teacher}
import data.req.{BioReq, WithdrawalReq}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import util.Util.rest


object TeacherRouter {

  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]


  def teacherRouter(teacherService: TeacherService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "new_lesson" => req.decodeJson[Lesson.Insert].flatMap(teacherService.createLesson(req, _))
        .forbidden

      case req@POST -> Root / "bio-update" => req.decodeJson[BioReq].flatMap(teacherService.bioUpdate(req, _))
        .forbidden

      case GET -> Root / "all_teachers" => teacherService.allTeachers
        .flatMap(res => Ok(res.asJson))

      case GET -> Root / "teacher" / LongVar(teacherId) => teacherService.findTeacher(teacherId)
        .notFound

      case req@GET -> Root / "upcoming-lessons" => teacherService.upcoming(req)
        .forbidden

      case req@GET -> Root / "next-lesson" => teacherService.next(req)
        .forbidden

      case req@GET -> Root / "previous-lessons" => teacherService.previous(req)
        .forbidden

      case req@GET -> Root / "lesson" / LongVar(lessonId) => teacherService.getYourLesson(req, lessonId)
        .forbidden

      case req@POST -> Root / "lesson" => req.decodeJson[Lesson].flatMap(teacherService.updateLesson(req, _)).forbidden

      case req@DELETE -> Root / "lesson" / LongVar(lessonId) => teacherService.delete(req, lessonId)
        .forbidden

      case req@POST -> Root / "withdrawal" => req.decodeJson[WithdrawalReq].flatMap(teacherService.withdrawal(req, _))
        .forbidden

      case req@POST -> Root / "payment" / LongVar(lessonId) => teacherService.payment(req, lessonId)
        .forbidden
    }
  }
}