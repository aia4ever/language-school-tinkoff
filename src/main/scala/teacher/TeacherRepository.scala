package teacher

import cats.effect.IO
import data.dao.TeacherDao
import data.dto.Lesson
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant


trait TeacherRepository {
  def deleteLesson(lessonId: Long, teacherId: Long): IO[Int]

  def updateLesson(lesson: Lesson, teacherId: Long): IO[Lesson]

  def getLesson(lessonId: Long, teacherId: Long): IO[Option[Lesson]]

  def upcomingLessons(teacherId: Long): IO[List[Lesson]]

  def previousLessons(teacherId: Long): IO[List[Lesson]]

  def newLesson(lesson: Lesson.Insert): IO[Lesson]

  def getLessonsByDate(userId: Long, date: Instant): IO[List[Lesson]]

  def getTeacher(teacherId: Long): IO[Option[TeacherDao]]

  def getAllTeachers: IO[List[TeacherDao]]

  def bioUpdate(id: Long, bio: String): IO[Int]

  def payment(lesson: Lesson): IO[Int]

  def updateLessonStatus(lessonId: Long, teacherId: Long): IO[Int]

  def teacherGrade(teacherId: Long): IO[Option[(Double, Int)]]
}


