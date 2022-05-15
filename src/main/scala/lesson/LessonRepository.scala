package lesson

import data.dto.Lesson
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant

trait LessonRepository {

  def teacherLesson(lessonId: Long, id: Long): ConnectionIO[Option[Lesson]]

  def upcomingLessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]]

  def previousLessonsByTeacher(id: Long): ConnectionIO[List[Lesson]]

  def lessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]]

  def emptyLesson(lessonId: Long): ConnectionIO[Option[Lesson]]

  def createLesson(insert: Lesson.Insert): ConnectionIO[Lesson]

  def updateLesson(lesson: Lesson, teacherId: Long): ConnectionIO[Lesson]

  def deleteLesson(lessonId: Long, teacherId: Long): ConnectionIO[Int]

  def studentLesson(lessonId: Long, studentId: Long): ConnectionIO[Option[Lesson]]

  def previousLessonsByStudent(id: Long): ConnectionIO[List[Lesson]]

  def upcomingLessonsByStudent(teacherId: Long): ConnectionIO[List[Lesson]]

  def signUp(lessonId: Long, studentId: Long): ConnectionIO[Lesson]

  def signOut(lessonId: Long, studentId: Long): ConnectionIO[Int]

  def homework(lessonId: Long, studentId: Long, homework: String): ConnectionIO[Lesson]

  def evaluateHomework(lessonId: Long, teacherId: Long, mark: Double): ConnectionIO[Lesson]

  def teacherLessonsByDate(teacherId: Long, date: Instant): ConnectionIO[List[Lesson]]

  def studentLessonsByDate(studentId: Long, date: Instant): ConnectionIO[List[Lesson]]
}
