package student

import cats.effect.IO
import data.dto.{Balance, Lesson}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import session.SessionRepository
import teacher.TeacherRepository
import user.UserRepository
import util.ApiErrors.{AccessDeniedError, InsufficientFundsError, InvalidSessionError, LessonNotFoundError, YouAreNotAStudentError}
import util.StudentType
import util.Implicits._

class StudentService(userRepository: UserRepository,
                     studentRepository: StudentRepository,
                     sessionRepository: SessionRepository,
                     teacherRepository: TeacherRepository
                    ) {
  def getLessonByTeacher(teacherId: Long): IO[List[Lesson]] = studentRepository.lessonsByTeacher(teacherId)

  def getLesson(lessonId: Long): IO[Lesson] = studentRepository.getLesson(lessonId).map {
      case Some(ls) => ls
      case None => throw LessonNotFoundError
  }

  def getYourLesson(session: String, lessonId: Long): IO[Lesson] = for {
    studentIdOpt <- sessionRepository.getIdBySession(session)
    studentId = studentIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    isStudent <- studentRepository.studentUserType(studentId).map(stringToUserType(_) == StudentType)
    lessonOpt <- if (isStudent) studentRepository.yourLesson(lessonId, studentId)
    else IO.raiseError(YouAreNotAStudentError)
    res = lessonOpt match {
      case Some(ls) => ls
      case None => throw LessonNotFoundError
    }
  } yield res

  def signUp(session: String, lessonId: Long): IO[Lesson] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isStudent <- studentRepository.studentUserType(studentId).map(stringToUserType(_) == StudentType)
      lessonOpt <- if (isStudent) studentRepository.getLesson(lessonId)
      else IO.raiseError(YouAreNotAStudentError)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw LessonNotFoundError
      }
      isNotBusy <- studentRepository.getLessonsByDate(studentId, lesson.date).map(_.isEmpty)
      balance <- userRepository.balance(studentId)
      res <- if (balance.amount > lesson.price && isNotBusy) {
        studentRepository.reserve(studentId, lesson.price)
        studentRepository.signUp(lessonId, studentId)
      }
      else IO.raiseError(InsufficientFundsError)
    } yield res


  def signOut(session: String, lessonId: Long): IO[Int] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      lesson <- studentRepository.studentLesson(lessonId, studentId)
      res <- {
        studentRepository.unreserve(studentId, lesson.price)
        studentRepository.signOut(lessonId, studentId)
      }
    } yield res

  def upcomingLessons(session: String): IO[List[Lesson]] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      res <- studentRepository.upcoming(studentId)
    } yield res

  def previousLessons(session: String): IO[List[Lesson]] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      res <- studentRepository.previous(studentId)
    } yield res

  def nextLesson(session: String): IO[Lesson] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      lessonOpt <- studentRepository.upcoming(studentId).map(_.headOption)
      res = lessonOpt match {
        case Some(ls) => ls
        case None => throw LessonNotFoundError
      }
    } yield res

  def evaluateTeacher(session: String, gradeReq: GradeReq): IO[Int] = for {
    studentIdOpt <- sessionRepository.getIdBySession(session)
    _ = studentIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    isTeacher <- teacherRepository.getTeacher(gradeReq.teacherId).map(_.fold(false)(_ => true))
    grade <- teacherRepository.teacherGrade(gradeReq.teacherId).map(_.fold((0.0, 0))(identity))
    newGrade = (grade._1 * grade._2 + gradeReq.rate) / (grade._2 + 1)
    res <- if (isTeacher) studentRepository.evaluateTeacherUpdate(gradeReq.teacherId, newGrade, grade._2 + 1)
    else IO.raiseError(AccessDeniedError)
  } yield res


  def cashIn(session: String, cashInReq: CashInReq): IO[Balance] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      res <- userRepository.cashIn(studentId, cashInReq.amount)
    } yield res

  def sendHomework(session: String, homeworkReq: HomeworkReq): IO[Lesson] =
    for {
      studentIdOpt <- sessionRepository.getIdBySession(session)
      studentId = studentIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      res <- studentRepository.homework(homeworkReq.lessonId, studentId, homeworkReq.homework)
    } yield res
}
