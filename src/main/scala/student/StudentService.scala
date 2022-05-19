package student

import cats.effect.IO
import data.dto.{Balance, Lesson}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import session.SessionRepository
import teacher.TeacherRepository
import user.UserRepository
import util.ApiErrors.{AccessDeniedError, InsufficientFundsError, YouAreNotAStudentError}

class StudentService(userRepository: UserRepository,
                     studentRepository: StudentRepository,
                     sessionRepository: SessionRepository,
                     teacherRepository: TeacherRepository
                    ) {
  def getLessonByTeacher(teacherId: Long): IO[List[Lesson]] = studentRepository.lessonsByTeacher(teacherId)

  def getLesson(lessonId: Long): IO[Lesson] = studentRepository.getLesson(lessonId)

  def getYourLesson(session: String, lessonId: Long): IO[Lesson] = for {
    studentId <- sessionRepository.getIdBySession(session)
    isStudent <- studentRepository.isStudent(studentId)
    res <- if (isStudent) studentRepository.yourLesson(lessonId, studentId)
    else IO.raiseError(YouAreNotAStudentError)
  } yield res

  def signUp(session: String, lessonId: Long): IO[Lesson] =
    for {

      studentId <- sessionRepository.getIdBySession(session)
      isStudent <- studentRepository.isStudent(studentId)
      lesson <- if (isStudent) studentRepository.getLesson(lessonId)
      else IO.raiseError(YouAreNotAStudentError)
      isNotBusy <- studentRepository.isNotBusy(studentId, lesson.date)
      balance <- userRepository.balance(studentId)
      res <- if (balance.amount > lesson.price && isNotBusy) {
        studentRepository.reserve(studentId, lesson.price)
        studentRepository.signUp(lessonId, studentId)
      }
      else IO.raiseError(InsufficientFundsError)
    } yield res


  def signOut(session: String, lessonId: Long): IO[Int] =
    for {

      studentId <- sessionRepository.getIdBySession(session)
      lesson <- studentRepository.studentLesson(lessonId, studentId)

      res <- {
        studentRepository.unreserve(studentId, lesson.price)
        studentRepository.signOut(lessonId, studentId)
      }
    } yield res

  def upcomingLessons(session: String): IO[List[Lesson]] =
    for {
      studentId <- sessionRepository.getIdBySession(session)
      res <- studentRepository.upcoming(studentId)
    } yield res

  def previousLessons(session: String): IO[List[Lesson]] =
    for {
      studentId <- sessionRepository.getIdBySession(session)
      res <- studentRepository.previous(studentId)
    } yield res

  def nextLesson(session: String): IO[Lesson] =
    for {
      studentId <- sessionRepository.getIdBySession(session)
      res <- studentRepository.next(studentId)
    } yield res

  def evaluateTeacher(session: String, gradeReq: GradeReq): IO[Int] = for {
    _ <- sessionRepository.getIdBySession(session)
    isTeacher <- teacherRepository.isTeacher(gradeReq.teacherId)
    grade <- teacherRepository.teacherGrade(gradeReq.teacherId)
    newGrade = (grade._1 * grade._2 + gradeReq.rate) / (grade._2 + 1)
    res <- if (isTeacher) studentRepository.evaluateTeacherUpdate(gradeReq.teacherId, newGrade, grade._2 + 1)
    else IO.raiseError(AccessDeniedError)
  } yield res


  def cashIn(session: String, cashInReq: CashInReq): IO[Balance] =
    for {
      id <- sessionRepository.getIdBySession(session)
      res <- userRepository.cashIn(id, cashInReq.amount)
    } yield res

  def sendHomework(session: String, homeworkReq: HomeworkReq): IO[Lesson] =
    for {
      id <- sessionRepository.getIdBySession(session)
      res <- studentRepository.homework(homeworkReq.lessonId, id, homeworkReq.homework)
    } yield res
}
