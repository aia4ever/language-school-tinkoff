package student

import cats.effect.IO
import data.dto.{Balance, Lesson}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import org.http4s.Request
import session.SessionRepository
import teacher.TeacherRepository
import user.UserRepository
import util.ApiErrors.{AccessDeniedError, InsufficientFundsError, YouAreNotAStudentError}
import util.Util.auth

class StudentService(userRepository: UserRepository,
                     studentRepository: StudentRepository,
                     sessionRepository: SessionRepository,
                     teacherRepository: TeacherRepository
                    ) {
  def getLessonByTeacher(teacherId: Long): IO[List[Lesson]] = studentRepository.lessonsByTeacher(teacherId)

  def getLesson(lessonId: Long): IO[Lesson] = studentRepository.getLesson(lessonId)

  def getYourLesson(req: Request[IO], lessonId: Long): IO[Lesson] = for {
    session <- auth(req)
    studentId <- sessionRepository.getIdBySession(session)
    isStudent <- studentRepository.isStudent(studentId)
    res <- if (isStudent) studentRepository.yourLesson(lessonId, studentId)
    else IO.raiseError(YouAreNotAStudentError)
  } yield res

  def signUp(req: Request[IO], lessonId: Long): IO[Lesson] =
    for {
      session <- auth(req)
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


  def signOut(req: Request[IO], lessonId: Long): IO[Int] =
    for {
      session <- auth(req)
      studentId <- sessionRepository.getIdBySession(session)
      lesson <- studentRepository.studentLesson(lessonId, studentId)

      res <- {
        studentRepository.unreserve(studentId, lesson.price)
        studentRepository.signOut(lessonId, studentId)
      }
    } yield res

  def upcomingLessons(req: Request[IO]): IO[List[Lesson]] =
    for {
      session <- auth(req)
      studentId <- sessionRepository.getIdBySession(session)
      res <- studentRepository.upcoming(studentId)
    } yield res

  def previousLessons(req: Request[IO]): IO[List[Lesson]] =
    for {
      session <- auth(req)
      studentId <- sessionRepository.getIdBySession(session)
      res <- studentRepository.previous(studentId)
    } yield res

  def nextLesson(req: Request[IO]): IO[Lesson] =
    for {
      session <- auth(req)
      studentId <- sessionRepository.getIdBySession(session)
      res <- studentRepository.next(studentId)
    } yield res

  def evaluateTeacher(req: Request[IO], gradeReq: GradeReq): IO[Int] = for {
    session <- auth(req)
    _ <- sessionRepository.getIdBySession(session)
    isTeacher <- teacherRepository.isTeacher(gradeReq.teacherId)
    grade <- teacherRepository.teacherGrade(gradeReq.teacherId)
    newGrade = (grade._1 * grade._2 + gradeReq.rate) / (grade._2 + 1)
    res <- if (isTeacher) studentRepository.evaluateTeacherUpdate(gradeReq.teacherId, newGrade, grade._2 + 1)
    else IO.raiseError(AccessDeniedError)
  } yield res


  def cashIn(req: Request[IO], cashInReq: CashInReq): IO[Balance] =
    for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      res <- userRepository.cashIn(id, cashInReq.amount)
    } yield res

  def sendHomework(req: Request[IO], homeworkReq: HomeworkReq): IO[Lesson] =
    for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      res <- studentRepository.homework(homeworkReq.lessonId, id, homeworkReq.homework)
    } yield res
}
