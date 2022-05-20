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

  def studentUserType(studentId: Long): IO[String] =
    StudentDictionary.studentUserType(studentId).transact(xa)

  def evaluateTeacherUpdate(teacherId: Long, grade: Double, amount: Int): IO[Int] =
    StudentDictionary.evaluateTeacherUpdate(teacherId)(grade, amount).transact(xa)

  def previous(studentId: Long): IO[List[Lesson]] = LessonDictionary.previousLessons(studentId, StudentType).transact(xa)

  def upcoming(studentId: Long): IO[List[Lesson]] = LessonDictionary.upcomingLessons(studentId, StudentType).transact(xa)

  def signUp(lessonId: Long, studentId: Long): IO[Lesson] =
    LessonDictionary.signUp(lessonId, studentId).transact(xa)

  def getLessonsByDate(userId:  Long, date: Instant): IO[List[Lesson]] =
    LessonDictionary.getLessonsByDate(userId, StudentType, date).transact(xa)

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

  def getLesson(lessonId: Long): IO[Option[Lesson]] = LessonDictionary.getEmptyLesson(lessonId).transact(xa)

  def yourLesson(lessonId: Long, studentId: Long): IO[Option[Lesson]] =
    LessonDictionary.getLesson(lessonId, studentId, StudentType).transact(xa)

  def homework(lessonId: Long, studentId: Long, homework: String): IO[Lesson] =
    LessonDictionary.homework(lessonId, studentId, homework).transact(xa)
}
