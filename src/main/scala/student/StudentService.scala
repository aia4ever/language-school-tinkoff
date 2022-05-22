package student

import cats.effect.IO
import data.dto.{Balance, Lesson, Teacher}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import session.SessionRepository
import teacher.TeacherRepository
import user.UserRepository
import util.ApiErrors._
import util.Implicits._
import util.StudentType

import java.time.Instant

class StudentService(userRepository: UserRepository,
                     studentRepository: StudentRepository,
                     sessionRepository: SessionRepository,
                     teacherRepository: TeacherRepository
                    ) {

  private def studentId(session: String): IO[Long] =
    sessionRepository.getIdBySession(session).map {
      case Some(id) => id
      case None => throw InvalidSessionError
    }

  private def isStudent(studentId: Long): IO[Boolean] =
    studentRepository.studentUserType(studentId).map(stringToUserType(_) == StudentType)

  def allTeachers: IO[List[Teacher]] = teacherRepository.getAllTeachers.map(_.map(_.toTeacher))

  def findTeacher(teacherId: Long): IO[Teacher] = teacherRepository.getTeacher(teacherId).map {
    case Some(teacher) => teacher.toTeacher
    case None => throw UserNotFoundError
  }

  def getLessonsByTeacher(teacherId: Long): IO[List[Lesson]] = studentRepository.lessonsByTeacher(teacherId)

  def getLesson(lessonId: Long): IO[Lesson] = studentRepository.getLesson(lessonId).map {
    case Some(ls) => ls
    case None => throw LessonNotFoundError
  }

  def getYourLesson(session: String, lessonId: Long): IO[Lesson] = for {
    studentId <- studentId(session)
    isStudent <- isStudent(studentId)
    lessonOpt <- if (isStudent) studentRepository.yourLesson(lessonId, studentId)
    else throw YouAreNotAStudentError
    res = lessonOpt match {
      case Some(ls) => ls
      case None => throw LessonNotFoundError
    }
  } yield res

  def signUp(session: String, lessonId: Long): IO[Lesson] =
    for {
      studentId <- studentId(session)
      isStudent <- isStudent(studentId)
      lessonOpt <- if (isStudent) studentRepository.getLesson(lessonId)
      else throw (YouAreNotAStudentError)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw LessonNotFoundError
      }
      isNotBusy <- studentRepository.getLessonsByDate(studentId, lesson.date).map(_.isEmpty)
      balance <- userRepository.balance(studentId)
      res <- if (balance.amount >= lesson.price && isNotBusy && lesson.date.isAfter(Instant.now())) {
        studentRepository.reserve(studentId, lesson.price)
        studentRepository.signUp(lessonId, studentId)
      }
      else IO.raiseError(AccessDeniedError)
    } yield res


  def signOut(session: String, lessonId: Long): IO[Int] =
    for {
      studentId <- studentId(session)
      lessonOpt <- studentRepository.studentLesson(lessonId, studentId)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw LessonNotFoundError
      }
      res <- {
        if (lesson.date.isAfter(Instant.now())) {
          studentRepository.unreserve(studentId, lesson.price)
          studentRepository.signOut(lessonId, studentId)
        }
        else IO.raiseError(AccessDeniedError)
      }
    } yield res

  def upcomingLessons(session: String): IO[List[Lesson]] =
    for {
      studentId <- studentId(session)
      res <- studentRepository.upcoming(studentId)
    } yield res

  def previousLessons(session: String): IO[List[Lesson]] =
    for {
      studentId <- studentId(session)
      res <- studentRepository.previous(studentId)
    } yield res

  def nextLesson(session: String): IO[Lesson] =
    for {
      studentId <- studentId(session)
      lessonList <- studentRepository.upcoming(studentId)
      res = if (lessonList.nonEmpty) lessonList.head
      else throw LessonNotFoundError
    } yield res

  def evaluateTeacher(session: String, gradeReq: GradeReq): IO[Int] = for {
    studentId <- studentId(session)
    isStudent <- isStudent(studentId)
    isTeacher <- teacherRepository.getTeacher(gradeReq.teacherId).map(_.fold(false)(_ => true))
    grade <- teacherRepository.teacherGrade(gradeReq.teacherId).map(_.fold((0.0, 0))(identity))
    newGrade = (grade._1 * grade._2 + gradeReq.rate) / (grade._2 + 1)
    res <- if (isTeacher && isStudent) studentRepository.evaluateTeacherUpdate(gradeReq.teacherId, newGrade, grade._2 + 1)
    else IO.raiseError(AccessDeniedError)
  } yield res


  def cashIn(session: String, cashInReq: CashInReq): IO[Balance] =
    for {
      studentId <- studentId(session)
      res <- userRepository.cashIn(studentId, cashInReq.amount)
    } yield res

  def sendHomework(session: String, homeworkReq: HomeworkReq): IO[Lesson] =
    for {
      studentId <- studentId(session)
      res <- if (studentId == homeworkReq.studentId) studentRepository.homework(homeworkReq.lessonId, studentId, homeworkReq.homework)
      else throw AccessDeniedError
    } yield res
}
