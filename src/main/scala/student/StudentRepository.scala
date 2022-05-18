package student

import cats.effect.IO
import data.dto.Lesson
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant

trait StudentRepository {

  def isStudent(studentId: Long): IO[Boolean]

  def evaluateTeacherUpdate(teacherId: Long, grade: Double, amount: Int): IO[Int]

  def next(studentId: Long): IO[Lesson]

  def previous(studentId: Long): IO[List[Lesson]]

  def upcoming(studentId: Long): IO[List[Lesson]]

  def signUp(lessonId: Long, studentId: Long): IO[Lesson]

  def isNotBusy(userId: Long, date: Instant): IO[Boolean]


  def reserve(studentId: Long, price: BigDecimal): IO[Int]

  def unreserve(studentId: Long, price: BigDecimal): IO[Int]

  def signOut(lessonId: Long, studentId: Long): IO[Int]

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]]

  def getLesson(lessonId: Long): IO[Lesson]

  def yourLesson(lessonId: Long, studentId: Long): IO[Lesson]

  def homework(lessonId: Long, studentId: Long, homework: String): IO[Lesson]

  def studentLesson(lessonId: Long, studentId: Long): IO[Lesson]
}


