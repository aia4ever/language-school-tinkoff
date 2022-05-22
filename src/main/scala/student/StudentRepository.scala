package student

import cats.effect.IO
import data.dto.Lesson
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant

trait StudentRepository {

  def studentUserType(studentId: Long): IO[String]

  def evaluateTeacherUpdate(teacherId: Long, grade: Double, amount: Int): IO[Int]

  def previous(studentId: Long): IO[List[Lesson]]

  def upcoming(studentId: Long): IO[List[Lesson]]

  def getLessonsByDate(userId:  Long, date: Instant): IO[List[Lesson]]

  def signUp(lessonId: Long, studentId: Long): IO[Lesson]

  def reserve(studentId: Long, price: BigDecimal): IO[Int]

  def unreserve(studentId: Long, price: BigDecimal): IO[Int]

  def signOut(lessonId: Long, studentId: Long): IO[Int]

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]]

  def getLesson(lessonId: Long): IO[Option[Lesson]]

  def yourLesson(lessonId: Long, studentId: Long): IO[Option[Lesson]]

  def homework(lessonId: Long, studentId: Long, homework: String): IO[Lesson]

  def studentLesson(lessonId: Long, studentId: Long): IO[Option[Lesson]]
}


