package teacher

import cats.effect.IO
import data.req.BioReq
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import lesson.Lesson
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityEncoder, HttpRoutes}
import session.SessionService
import util.Util.{auth, rest}


object TeacherRouter {

  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]


  def teacherRouter(teacherService: TeacherService, sessionService: SessionService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "new_lesson" =>
        (for {
          session <- auth(req)
          request <- req.decodeJson[Lesson.Insert]
          id <- sessionService.checkSession(session)
          is <- teacherService.isTeacher(id)
          res <- if (is) teacherService.newLesson(request, id)
          else IO.raiseError(throw new Exception("You are not a Teacher"))
        } yield res).forbidden

      case req@POST -> Root / "bio-update" => (for {
        session <- auth(req)
        request <- req.decodeJson[BioReq]
        id <- sessionService.checkSession(session)
        is <- teacherService.isTeacher(id)
        res <- if (is) teacherService.insertOrUpdateBio(id, request.bio)
        else IO.raiseError(throw new Exception("You are not a Teacher"))
      } yield res).forbidden

      case GET -> Root / "all_teachers" => teacherService.allTeachers().flatMap(res => Ok(res.asJson))

      case GET -> Root / LongVar(id) => teacherService.findTeacher(id)
        .notFound

      case req@GET -> Root / "upcoming-lessons" =>
        (for {
          session <- auth(req)
          id <- sessionService.checkSession(session)
          res <- teacherService.upcomingLessons(id)
        } yield res).forbidden

      case req@GET -> Root / "next-lesson"  => (for {
        session <- auth(req)
        id <- sessionService.checkSession(session)
        res <- teacherService.nextLesson(id)
      } yield res).forbidden

      case req@GET -> Root / "previous-lessons" / session => (for {
        session <- auth(req)
        id <- sessionService.checkSession(session)
        res <- teacherService.previousLessons(id)
      } yield res).forbidden

      case req@GET -> Root / "lesson" / LongVar(id) => (for {
        session <- auth(req)
        _id <- sessionService.checkSession(session)
        res <- teacherService.getLesson(id, _id)
      } yield res).forbidden

      case req@POST -> Root / "lesson"  => (for {
        session <- auth(req)
        request <- req.decodeJson[Lesson]
        id <- sessionService.checkSession(session)
        res <- teacherService.updateLesson(request, id)
      } yield res).forbidden

      case DELETE -> Root / "lesson" / LongVar(id) / session =>
        (for {
          teacherId <- sessionService.checkSession(session)
          res <- teacherService.deleteLesson(id, teacherId)
        } yield res).forbidden

      case req@POST -> Root / "withdrawal" / session => (for {
        request <- req.decodeJson[Double]
        id <- sessionService.checkSession(session)
        res <- teacherService.withdrawal(id, request)
      } yield res).forbidden
    }
  }
}