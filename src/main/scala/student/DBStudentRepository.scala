package student

import cats.effect.IO
import config.DI.ResTransactor
import data.dto.Lesson
import doobie.implicits._
import util.ApiErrors._
import util.StudentType
import util.Util.dbConnection
import util.dictionary.{LessonDictionary, StudentDictionary, UserDictionary}

import java.time.Instant


class DBStudentRepository(xa: ResTransactor[IO]) extends StudentRepository {

  def studentUserType(studentId: Long): IO[String] =
    StudentDictionary.studentUserType(studentId).cast(xa)

  def evaluateTeacherUpdate(teacherId: Long, grade: Double, amount: Int): IO[Int] =
    StudentDictionary.evaluateTeacherUpdate(teacherId)(grade, amount).cast(xa)

  def previous(studentId: Long): IO[List[Lesson]] = LessonDictionary.previousLessons(studentId, StudentType).cast(xa)

  def upcoming(studentId: Long): IO[List[Lesson]] = LessonDictionary.upcomingLessons(studentId, StudentType).cast(xa)

  def signUp(lessonId: Long, studentId: Long): IO[Lesson] =
    LessonDictionary.signUp(lessonId, studentId).cast(xa)

  def getLessonsByDate(userId:  Long, date: Instant): IO[List[Lesson]] =
    LessonDictionary.getLessonsByDate(userId, StudentType, date).cast(xa)

  def reserve(studentId: Long, price: BigDecimal): IO[Int] = UserDictionary.reserve(studentId, price).cast(xa)

  def unreserve(studentId: Long, price: BigDecimal): IO[Int] = UserDictionary.unreserve(studentId, price).cast(xa)

  def signOut(lessonId: Long, studentId: Long): IO[Int] = LessonDictionary.signOut(lessonId, studentId).cast(xa)


  def studentLesson(lessonId: Long, studentId: Long): IO[Option[Lesson]] =
    LessonDictionary.studentLesson(lessonId, studentId).cast(xa)

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]] =
    LessonDictionary.lessonsByTeacher(teacherId).cast(xa)

  def getLesson(lessonId: Long): IO[Option[Lesson]] = LessonDictionary.getEmptyLesson(lessonId).cast(xa)

  def yourLesson(lessonId: Long, studentId: Long): IO[Option[Lesson]] =
    LessonDictionary.getLesson(lessonId, studentId, StudentType).cast(xa)

  def homework(lessonId: Long, studentId: Long, homework: String): IO[Lesson] =
    LessonDictionary.homework(lessonId, studentId, homework).cast(xa)
}
