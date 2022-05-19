package teacher

import cats.effect.IO
import data.dto.{Balance, Lesson, Teacher}
import data.req.{BioReq, WithdrawalReq}
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
  def createLesson(session: String, insertReq: Lesson.Insert): IO[Lesson] =
    for {
      id <- sessionRepository.getIdBySession(session)
      isTeacher <- teacherRepository.isTeacher(id)
      isNotBusy <- if (isTeacher) teacherRepository.isNotBusy(id, insertReq.date)
      else IO.raiseError(YouAreNotATeachError)
      res <- if (isNotBusy) teacherRepository.newLesson(insertReq)
      else IO.raiseError(BusyError)
    } yield res

  def bioUpdate(session: String, bioReq: BioReq): IO[Teacher] =
    for {
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

  def upcoming(session: String): IO[List[Lesson]] = for {
    id <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.upcomingLessons(id)
  } yield res

  def next(session: String): IO[Lesson] = for {
    id <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.nextLesson(id)
  } yield res

  def previous(session: String): IO[List[Lesson]] = for {
    id <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.previousLessons(id)
  } yield res

  def getYourLesson(session: String, lessonId: Long): IO[Lesson] = for {
    teacherId <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.getLesson(lessonId, teacherId)
  } yield res

  def updateLesson(session: String, lesson: Lesson): IO[Lesson] =
    for {
      teacherId <- sessionRepository.getIdBySession(session)
      res <- teacherRepository.updateLesson(lesson, teacherId)
    } yield res

  def delete(session: String, lessonId: Long): IO[Int] = for {
    teacherId <- sessionRepository.getIdBySession(session)
    res <- teacherRepository.deleteLesson(lessonId, teacherId)
  } yield res

  def withdrawal(session: String, withdrawalReq: WithdrawalReq): IO[Balance] = for {
    teacherId <- sessionRepository.getIdBySession(session)
    balance <- userRepository.balance(teacherId)
    res <- if (balance.amount >= withdrawalReq.amount) userRepository.withdrawal(teacherId, withdrawalReq.amount)
    else IO.raiseError(InsufficientFundsError)
  } yield res

  def payment(session: String, lessonId: Long): IO[Int] =
    for {
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
