package teacher

import cats.effect.IO
import data.dto.{Balance, Lesson, Teacher}
import data.req.{BioReq, WithdrawalReq}
import org.http4s.Request
import session.SessionRepository
import user.UserRepository
import util.ApiErrors._
import util.Util.auth

import java.time.Instant
import java.time.temporal.ChronoUnit

class TeacherService(
                      teacherRepository: TeacherRepository,
                      userRepository: UserRepository,
                      sessionRepository: SessionRepository
                    ) {
  def createLesson(req: Request[IO], insertReq: Lesson.Insert): IO[Lesson] =
    (for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      isTeacher <- teacherRepository.isTeacher(id)
      isNotBusy <- if (isTeacher) teacherRepository.isNotBusy(id, insertReq.date)
      else IO.raiseError(YouAreNotATeachError)
      res <- if (isNotBusy) teacherRepository.newLesson(insertReq)
      else IO.raiseError(BusyError)
    } yield res)

  def bioUpdate(req: Request[IO], bioReq: BioReq): IO[Teacher] =
    for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      isTeacher <- teacherRepository.isTeacher(id)
      res <- if (isTeacher) {
        teacherRepository.bioUpdate(id, bioReq.bio)
        teacherRepository.getTeacher(id)
      }
      else IO.raiseError(YouAreNotATeachError)
    } yield res

  def allTeachers: IO[List[Teacher]] = teacherRepository.getAllTeachers

  def findTeacher(teacherId: Long): IO[Teacher] = teacherRepository.getTeacher(teacherId)

  def upcoming(req: Request[IO]): IO[List[Lesson]] = for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.upcomingLessons(id)
  } yield res

  def next(req: Request[IO]): IO[Lesson] = for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.nextLesson(id)
  } yield res

  def previous(req: Request[IO]): IO[List[Lesson]] = for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.previousLessons(id)
  } yield res

  def getYourLesson(req: Request[IO], lessonId: Long): IO[Lesson] = for {
    session <- auth(req)
    teacherId <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.getLesson(lessonId, teacherId)
  } yield res

  def updateLesson(req: Request[IO], lesson: Lesson): IO[Lesson] =
    for {
      session <- auth(req)
      teacherId <- sessionRepository.getIdBySession(session)
      res <- teacherRepository.updateLesson(lesson, teacherId)
    } yield res

  def delete(req: Request[IO], lessonId: Long): IO[Int] = for {
    session <- auth(req)
    teacherId <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.deleteLesson(lessonId, teacherId)
  } yield res

  def withdrawal(req: Request[IO], withdrawalReq: WithdrawalReq): IO[Balance] = for {
    session <- auth(req)
    teacherId <- sessionRepository.getIdBySession(session)
    balance <- userRepository.balance(teacherId)
    res <- if (balance.amount >= withdrawalReq.amount) userRepository.withdrawal(teacherId, withdrawalReq.amount)
    else IO.raiseError(InsufficientFundsError)
  } yield res

  def payment(req: Request[IO], lessonId: Long): IO[Int] =
    for {
      session <- auth(req)
      teacherId <- sessionRepository.getIdBySession(session)
      isTeacher <- teacherRepository.isTeacher(teacherId)
      lesson <- if (isTeacher) teacherRepository.getLesson(lessonId, teacherId)
      else IO.raiseError(YouAreNotATeachError)
      studentId = lesson.studentId.fold(0L)(identity)
      isPurchased = lesson.purchased
      res <- if (studentId != 0 && !isPurchased && lesson.date.plus(1, ChronoUnit.HOURS).isBefore(Instant.now())) {
        teacherRepository.updateLessonStatus(lessonId, teacherId)
        teacherRepository.payment(lesson)
      } else IO.raiseError(AccessDeniedError)
    } yield res

}
