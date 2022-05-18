package student

import cats.effect.IO
import cats.effect.IO.asyncForIO
import data.dto.Lesson
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors._
import util.StudentType
import util.dictionary.{LessonDictionary, StudentDictionary, UserDictionary}

import java.time.Instant


class DBStudentRepository(xa: Aux[IO, Unit]) extends StudentRepository {

  def isStudent(studentId: Long): IO[Boolean] =
    StudentDictionary.studentUserType(studentId).transact(xa).map {
      case Some(ut) if ut == "StudentType" => true
      case _ => false
    }

  def evaluateTeacherUpdate(teacherId: Long, grade: Double, amount: Int): IO[Int] =
    StudentDictionary.evaluateTeacherUpdate(teacherId)(grade, amount).transact(xa)


  def next(studentId: Long): IO[Lesson] =
    LessonDictionary.upcomingLessons(studentId, StudentType).transact(xa).map(_.headOption match {
      case Some(ls) => ls
      case None => throw NoLessonError
    })

  def previous(studentId: Long): IO[List[Lesson]] = LessonDictionary.previousLessons(studentId, StudentType).transact(xa)

  def upcoming(studentId: Long): IO[List[Lesson]] = LessonDictionary.upcomingLessons(studentId, StudentType).transact(xa)

  def signUp(lessonId: Long, studentId: Long): IO[Lesson] =
    LessonDictionary.signUp(lessonId, studentId).transact(xa)

  def isNotBusy(userId:  Long, date: Instant): IO[Boolean] =
    LessonDictionary.getLessonsByDate(userId, StudentType, date).transact(xa).map(_.isEmpty)

  def reserve(studentId: Long, price: BigDecimal): IO[Int] = UserDictionary.reserve(studentId, price).transact(xa)

  def unreserve(studentId: Long, price: BigDecimal): IO[Int] = UserDictionary.unreserve(studentId, price).transact(xa)

  def signOut(lessonId: Long, studentId: Long): IO[Int] = LessonDictionary.signOut(lessonId, studentId).transact(xa)


  def studentLesson(lessonId: Long, studentId: Long): IO[Lesson] =
    LessonDictionary.studentLesson(lessonId, studentId).transact(xa).map {
    case Some(ls) => ls
    case None => throw SomethingWentWrongError
  }

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]] =
    LessonDictionary.lessonsByTeacher(teacherId).transact(xa)

  def getLesson(lessonId: Long): IO[Lesson] = LessonDictionary.getEmptyLesson(lessonId).transact(xa).map {
    case Some(ls) => ls
    case None => throw SomethingWentWrongError
  }

  def yourLesson(lessonId: Long, studentId: Long): IO[Lesson] =
    LessonDictionary.getLesson(lessonId, studentId, StudentType).transact(xa).map {
      case Some(ls) => ls
      case None => throw SomethingWentWrongError
    }

  def homework(lessonId: Long, studentId: Long, homework: String): IO[Lesson] =
    LessonDictionary.homework(lessonId, studentId, homework).transact(xa)
}
