package student

import cats.effect.IO
import data.req.GradeReq
import io.circe.generic.auto._
import io.circe.syntax._
import lesson.Lesson
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import session.SessionService
import teacher.{Teacher, TeacherService}
import util.Implicits._
import util.Util.rest

object StudentRouter {
  implicit val studentEncoder: EntityEncoder[IO, Student] = jsonEncoderOf[IO, Student]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]
  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val rateReq: EntityEncoder[IO, GradeReq] = jsonEncoderOf[IO, GradeReq]


  def studentRouter(implicit studentService: StudentService, sessionService: SessionService, teacherService: TeacherService): HttpRoutes[IO] = {
    HttpRoutes.of {

      case GET -> Root / "lessons" / LongVar(teacher) => studentService.lessonsByTeacher(teacher)
        .flatMap(res => Ok(res.asJson))

      case GET -> Root / "lesson" / LongVar(id) => studentService.lesson(id)
        .forbidden

      case GET -> Root / "your-lesson" / LongVar(id) / session =>
        (for {
          _id <- sessionService.checkSession(session)
          is <- studentService.isStudent(_id)
          res <- if (is) studentService.yourLesson(id, _id)
          else IO.raiseError(throw new Exception("You are not a Student"))
        } yield res).forbidden


      case POST -> Root / "lesson" / LongVar(id) / "sign-up" / session =>
        (for {
          studentId <- sessionService.checkSession(session)
          _ <- studentService.lesson(id)
          is <- studentService.isStudent(studentId)
          res <- if (is) studentService.signUp(id, studentId)
          else IO.raiseError(throw new Exception("You are not a Student"))
        } yield res).forbidden


      case POST -> Root / "lesson" / LongVar(id) / "sign-out" / session => sessionService.checkSession(session)
        .flatMap(studentService.signOut(id, _)).flatMap(res => Ok(res.asJson))

      case GET -> Root / "upcoming-lessons" / session => sessionService.checkSession(session)
        .flatMap(studentService.upcoming).flatMap(res => Ok(res.asJson))

      case GET -> Root / "previous-lessons" / session => sessionService.checkSession(session)
        .flatMap(studentService.previous).flatMap(res => Ok(res.asJson))

      case GET -> Root / "next" / session => sessionService.checkSession(session)
        .flatMap(studentService.next).forbidden

      case req@POST -> Root / "evaluate-teacher" / session =>
        (for {
          request <- req.decodeJson[GradeReq]
          _ <- sessionService.checkSession(session)
          is <- teacherService.isTeacher(request.teacherId)
          res <- if (is) studentService.evaluateTeacherUpdate(request)
          else IO.raiseError(throw new Exception("You are trying evaluate not the teacher"))
        } yield res).forbidden

      case req@POST -> Root / "cash-in" / session =>
        (for {
          request <- req.decodeJson[Double]
          _ <- sessionService.checkSession(session)
          res <- studentService.cashIn(request)
        } yield res).forbidden

    }
  }
}


//Ученик:
//
//  1. Регистрация
//  2. Список всех учителей

//  4. Список всех доступных занятий конкретного преподавателя
//  5. Пополнение внутреннего кошелька
//  6. Запись на доступное занятие
//  7. Получение доступа к занятию
//  8. Выполнение домашнего задания
//  9. Оценка работы преподавателя
//  10. Удаление аккаунта
//object TeacherRouter {
//
//  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO ,Teacher]
//
//
//  def teacherRouter(implicit teacherService: TeacherService): HttpRoutes[IO]  = {
//    HttpRoutes.of {
//      case req@POST -> Root / "new_lesson" as _ => req.decodeJson[Lesson]
//        .flatMap(teacherService.newLesson)
//        .flatMap(res => Ok(res.asJson))
//
//      case req@POST -> Root / "bio-update" as _ => req.decodeJson[String]
//        .flatMap(teacherService.insertOrUpdateBio)
//        .flatMap(res => Ok(res.asJson))
//
//      case GET -> Root / "upcoming-lessons" as _ => teacherService.upcomingLessons().flatMap(res => Ok(res.asJson))
//
//      case GET -> Root / "next-lesson" as _ => teacherService.upcomingLessons().flatMap(res => Ok(res.asJson))
//
//      case GET -> Root / "previous-lessons" as _ => teacherService.previousLessons().flatMap(res => Ok(res.asJson))
//
//      case GET -> Root / "lesson" / id as _ => teacherService.lesson(id).flatMap(res => Ok(res.asJson))
//
//      case req@POST -> Root / "lesson" / id as _ => req.decodeJson[Lesson]
//        .flatMap(teacherService.lessonUpdate(_,id)).flatMap(res => Ok(res.asJson))
//
//      case DELETE -> Root / "lesson" / id as _ => teacherService.deleteLesson(id).flatMap(res => Ok(res.asJson))
//
//      case req@POST -> Root / "withdrawal" as _ => req.decodeJson[Double].flatMap(teacherService.withdrawal).flatMap(res => Ok(res.asJson))
//    }
//  }
//}