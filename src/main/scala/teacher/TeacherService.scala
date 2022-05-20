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
      teacherIdOpt <- sessionRepository.getIdBySession(session)
      teacherId = teacherIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isTeacher <- teacherRepository.getTeacher(teacherId).map(_.fold(false)(_ => true))
      activity <- if (isTeacher) teacherRepository.getLessonsByDate(teacherId, insertReq.date)
      else IO.raiseError(YouAreNotATeachError)
      isNotBusy = activity.isEmpty
      res <- if (isNotBusy) teacherRepository.newLesson(insertReq)
      else IO.raiseError(BusyError)
    } yield res

  def bioUpdate(session: String, bioReq: BioReq): IO[Teacher] =
    for {
      teacherIdOpt <- sessionRepository.getIdBySession(session)
      teacherId = teacherIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isTeacher <- teacherRepository.getTeacher(teacherId).map(_.fold(false)(_ => true))
      teacherOpt <- if (isTeacher) {
        teacherRepository.bioUpdate(teacherId, bioReq.bio)
        teacherRepository.getTeacher(teacherId)
      }
      else IO.raiseError(YouAreNotATeachError)
      res = teacherOpt match {
        case Some(teacher) => teacher.toTeacher
        case None => throw UserNotFoundError
      }
    } yield res

  def allTeachers: IO[List[Teacher]] = teacherRepository.getAllTeachers.map(_.map(_.toTeacher))

  def findTeacher(teacherId: Long): IO[Teacher] = teacherRepository.getTeacher(teacherId).map{
    case Some(teacher) => teacher.toTeacher
    case None => throw UserNotFoundError
  }

  def upcoming(session: String): IO[List[Lesson]] = for {
    teacherIdOpt <- sessionRepository.getIdBySession(session)
    teacherId = teacherIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    res <- teacherRepository.upcomingLessons(teacherId)
  } yield res

  def next(session: String): IO[Lesson] = for {
    teacherIdOpt <- sessionRepository.getIdBySession(session)
    teacherId = teacherIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    list <- teacherRepository.upcomingLessons(teacherId)
    res = list.headOption match {
      case Some(ls) => ls
      case None => throw NoLessonError
    }
  } yield res

  def previous(session: String): IO[List[Lesson]] = for {
    teacherIdOpt <- sessionRepository.getIdBySession(session)
    teacherId = teacherIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    res <- teacherRepository.previousLessons(teacherId)
  } yield res

  def getYourLesson(session: String, lessonId: Long): IO[Lesson] = for {
    teacherIdOpt <- sessionRepository.getIdBySession(session)
    teacherId = teacherIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    lessonOpt <- teacherRepository.getLesson(lessonId, teacherId)
    res = lessonOpt match {
      case Some(ls) => ls
      case None => throw AccessDeniedError
    }
  } yield res

  def updateLesson(session: String, lesson: Lesson): IO[Lesson] =
    for {
      teacherIdOpt <- sessionRepository.getIdBySession(session)
      teacherId = teacherIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      res <- teacherRepository.updateLesson(lesson, teacherId)
    } yield res

  def delete(session: String, lessonId: Long): IO[Int] = for {
    teacherIdOpt <- sessionRepository.getIdBySession(session)
    teacherId = teacherIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    res <- teacherRepository.deleteLesson(lessonId, teacherId)
  } yield res

  def withdrawal(session: String, withdrawalReq: WithdrawalReq): IO[Balance] = for {
    teacherIdOpt <- sessionRepository.getIdBySession(session)
    teacherId = teacherIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    balance <- userRepository.balance(teacherId)
    res <- if (balance.amount >= withdrawalReq.amount) userRepository.withdrawal(teacherId, withdrawalReq.amount)
    else IO.raiseError(InsufficientFundsError)
  } yield res

  def payment(session: String, lessonId: Long): IO[Int] =
    for {
      teacherIdOpt <- sessionRepository.getIdBySession(session)
      teacherId = teacherIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isTeacher <- teacherRepository.getTeacher(teacherId).map(_.fold(false)(_ => true))
      lessonOpt <- if (isTeacher) teacherRepository.getLesson(lessonId, teacherId)
      else IO.raiseError(YouAreNotATeachError)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw LessonNotFoundError
      }
      studentId = lesson.studentId.fold(0L)(identity)
      isPurchased = lesson.purchased
      res <- if (studentId != 0 && !isPurchased &&
        lesson.date.plus(1, ChronoUnit.HOURS).isBefore(Instant.now())) {
        teacherRepository.updateLessonStatus(lessonId, teacherId)
        teacherRepository.payment(lesson)
      } else IO.raiseError(AccessDeniedError)
    } yield res

}
