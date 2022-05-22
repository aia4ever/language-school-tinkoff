package teacher

import cats.effect.IO
import data.dto.{Balance, Lesson, Teacher}
import data.req.{BioReq, LessonUpdateReq, WithdrawalReq}
import session.SessionRepository
import user.UserRepository
import util.ApiErrors._

import java.time.Instant
import java.time.temporal.ChronoUnit

class TeacherService(
                      teacherRepository: TeacherRepository,
                      userRepository: UserRepository,
                      sessionRepository: SessionRepository
                    ) {

  private def teacherId(session: String): IO[Long] =
    sessionRepository.getIdBySession(session).map {
      case Some(id) => id
      case None => throw InvalidSessionError
    }

  private def isTeacher(teacherId: Long): IO[Boolean] =
    teacherRepository.getTeacher(teacherId).map(_.fold(false)(_ => true))

  private def isNotBusy(teacherId: Long, date: Instant): IO[Boolean] =
    teacherRepository.getLessonsByDate(teacherId, date).map(_.isEmpty)

  def createLesson(session: String, insertReq: Lesson.Insert): IO[Lesson] =
    for {
      teacherId <- teacherId(session)
      isTeacher <- isTeacher(teacherId)
      isNotBusy <- if (isTeacher) isNotBusy(teacherId, insertReq.date)
      else throw (YouAreNotATeachError)
      res <- if (isNotBusy) teacherRepository.newLesson(insertReq)
      else IO.raiseError(BusyError)
    } yield res

  def bioUpdate(session: String, bioReq: BioReq): IO[Teacher] =
    for {
      teacherId <- teacherId(session)
      isTeacher <- isTeacher(teacherId)
      _ <- if (isTeacher) teacherRepository.bioUpdate(teacherId, bioReq.bio)
      else throw (YouAreNotATeachError)
      res <- teacherRepository.getTeacher(teacherId)
    } yield res.map(_.toTeacher).get


  def upcoming(session: String): IO[List[Lesson]] = for {
    teacherId <- teacherId(session)
    res <- teacherRepository.upcomingLessons(teacherId)
  } yield res

  def next(session: String): IO[Lesson] = for {
    teacherId <- teacherId(session)
    list <- teacherRepository.upcomingLessons(teacherId)
    res = list.headOption match {
      case Some(ls) => ls
      case None => throw NoLessonError
    }
  } yield res

  def previous(session: String): IO[List[Lesson]] = for {
    teacherId <- teacherId(session)
    res <- teacherRepository.previousLessons(teacherId)
  } yield res

  def getYourLesson(session: String, lessonId: Long): IO[Lesson] = for {
    teacherId <- teacherId(session)
    lessonOpt <- teacherRepository.getLesson(lessonId, teacherId)
    res = lessonOpt match {
      case Some(ls) => ls
      case None => throw AccessDeniedError
    }
  } yield res

  def updateLesson(session: String, lesson: LessonUpdateReq): IO[Lesson] =
    for {
      teacherId <- teacherId(session)
      _lesson <- teacherRepository.getLesson(lesson.id, teacherId).map {
        case Some(ls) => ls
        case None => throw NoLessonError
      }
      res <-  if (_lesson.teacherId == teacherId) teacherRepository.updateLesson(lesson)
      else throw AccessDeniedError
    } yield res

  def delete(session: String, lessonId: Long): IO[Int] = for {
    teacherId <- teacherId(session)
    res <- teacherRepository.deleteLesson(lessonId, teacherId)
  } yield res

  def withdrawal(session: String, withdrawalReq: WithdrawalReq): IO[Balance] = for {
    teacherId <- teacherId(session)
    balance <- userRepository.balance(teacherId)
    res <- if (balance.amount >= withdrawalReq.amount) userRepository.withdrawal(teacherId, withdrawalReq.amount)
    else IO.raiseError(InsufficientFundsError)
  } yield res

  def payment(session: String, lessonId: Long): IO[Int] =
    for {
      teacherId <- teacherId(session)
      isTeacher <- isTeacher(teacherId)
      lessonOpt <- if (isTeacher) teacherRepository.getLesson(lessonId, teacherId)
      else IO.raiseError(YouAreNotATeachError)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw LessonNotFoundError
      }
      studentId = lesson.studentId.fold(0L)(identity)
      isPurchased = lesson.purchased
      _ <- if (studentId != 0 && !isPurchased &&
        lesson.date.plus(1, ChronoUnit.HOURS).isBefore(Instant.now()))
        teacherRepository.updateLessonStatus(lessonId, teacherId)
      else IO.raiseError(AccessDeniedError)
      res <- teacherRepository.payment(lesson)
    } yield res

}
