package student

import cats.effect.IO
import data.dto.{Lesson, Student, Teacher}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import util.Util.{auth, rest}

object StudentRouter {
  implicit val studentEncoder: EntityEncoder[IO, Student] = jsonEncoderOf[IO, Student]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]
  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val rateReq: EntityEncoder[IO, GradeReq] = jsonEncoderOf[IO, GradeReq]


  def studentRouter(studentService: StudentService): HttpRoutes[IO] = {
    HttpRoutes.of {

      case GET -> Root / "all_teachers" => studentService.allTeachers
        .flatMap(res => Ok(res.asJson))

      case GET -> Root / "teacher" / LongVar(teacherId) => studentService.findTeacher(teacherId)
        .notFound

      case GET -> Root / "lessons" / LongVar(teacherId) => studentService.getLessonsByTeacher(teacherId)
        .flatMap(res => Ok(res.asJson))

      case GET -> Root / "lesson" / LongVar(lessonId) => studentService.getLesson(lessonId)
        .forbidden

      case req@GET -> Root / "your-lesson" / LongVar(lessonId) => auth(req).flatMap( studentService.getYourLesson(_, lessonId))
        .forbidden

      case req@POST -> Root / "lesson" / "homework" => req.decodeJson[HomeworkReq]
        .flatMap( request => auth(req).flatMap(studentService.sendHomework(_, request))).forbidden

      case req@POST -> Root / "lesson" / LongVar(lessonId) / "sign-up" =>
        auth(req).flatMap(studentService.signUp(_, lessonId))
        .forbidden

      case req@POST -> Root / "lesson" / LongVar(lessonId) / "sign-out" =>
        auth(req).flatMap( studentService.signOut(_, lessonId))
        .forbidden

      case req@GET -> Root / "upcoming-lessons" =>
        auth(req).flatMap(studentService.upcomingLessons)
        .forbidden

      case req@GET -> Root / "previous-lessons" => auth(req).flatMap(studentService.previousLessons)
        .forbidden

      case req@GET -> Root / "next" => auth(req).flatMap(studentService.nextLesson)
        .forbidden

      case req@POST -> Root / "evaluate-teacher" => req.decodeJson[GradeReq]
        .flatMap(request => auth(req).flatMap(studentService.evaluateTeacher(_, request)))
        .forbidden

      case req@POST -> Root / "cash-in" => req.decodeJson[CashInReq]
        .flatMap(request => auth(req).flatMap(studentService.cashIn(_, request)))
        .forbidden

    }
  }
}