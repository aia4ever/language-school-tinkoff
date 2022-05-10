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
import util.Util.rest



object TeacherRouter {

  implicit val teacherEncoder: EntityEncoder[IO, Teacher] = jsonEncoderOf[IO, Teacher]
  implicit val lessonEncoder: EntityEncoder[IO, Lesson] = jsonEncoderOf[IO, Lesson]


  def teacherRouter(implicit teacherService: TeacherService, sessionService: SessionService): HttpRoutes[IO] = {
    HttpRoutes.of {
      case req@POST -> Root / "new_lesson" / session  =>
        (for {
          request <- req.decodeJson[Lesson.Insert]
          id <- sessionService.checkSession(session)
          is <- teacherService.isTeacher(id)
          res <- if (is) teacherService.newLesson(request, id)
          else IO.raiseError(throw new Exception("You are not a Teacher"))
        } yield res).forbidden

      case req@POST -> Root / "bio-update" / session => (for {
        request <- req.decodeJson[BioReq]
        id <- sessionService.checkSession(session)
        is <- teacherService.isTeacher(id)
        res <-  if (is) teacherService.insertOrUpdateBio(id, request.bio)
        else IO.raiseError(throw new Exception("You are not a Teacher"))
      } yield res).forbidden

      case GET -> Root / "all_teachers"  => teacherService.allTeachers().flatMap(res => Ok(res.asJson))

      case GET -> Root / LongVar(id)  => teacherService.findTeacher(id)
        .notFound

      case GET -> Root / "upcoming-lessons" / session  => sessionService.checkSession(session)
        .flatMap{ teacherService.upcomingLessons }.forbidden

      case GET -> Root / "next-lesson" / session => sessionService.checkSession(session)
        .flatMap { teacherService.nextLesson }.forbidden

      case GET -> Root / "previous-lessons" / session  => sessionService.checkSession(session)
        .flatMap{ teacherService.previousLessons }.forbidden

      case GET -> Root / "lesson" / LongVar(id)/ session  => sessionService.checkSession(session).flatMap{
        teacherService.getLesson(id, _)}.forbidden

      case req@POST -> Root / "lesson" / session => (for {
        request <- req.decodeJson[Lesson]
        id <- sessionService.checkSession(session)
        res  <- teacherService.updateLesson(request, id)
      } yield res).forbidden

      case DELETE -> Root / "lesson" / LongVar(id) / session  =>
        (for {
          teacherId <- sessionService.checkSession(session)
          res <- teacherService.deleteLesson(id, teacherId)
        } yield res).forbidden

      case req@POST -> Root / "withdrawal" / session  => (for {
        request <- req.decodeJson[Double]
        id <- sessionService.checkSession(session)
        res <- teacherService.withdrawal(id, request)
      } yield res).forbidden
    }
  }
}