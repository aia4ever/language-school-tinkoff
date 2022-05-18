package teacher

import cats.effect.IO
import data.dto.{Lesson, Teacher}
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant


trait TeacherRepository {
  def deleteLesson(lessonId: Long, teacherId: Long): IO[Int]

  def updateLesson(lesson: Lesson, teacherId: Long): IO[Lesson]

  def getLesson(lessonId: Long, teacherId: Long): IO[Lesson]

  def isTeacher(id: Long): IO[Boolean]

  def upcomingLessons(teacherId: Long): IO[List[Lesson]]

  def nextLesson(teacherId: Long): IO[Lesson]

  def previousLessons(teacherId: Long): IO[List[Lesson]]

  def newLesson(lesson: Lesson.Insert): IO[Lesson]

  def isNotBusy(userId: Long, date: Instant): IO[Boolean]

  def getTeacher(teacherId: Long): IO[Teacher]

  def getAllTeachers: IO[List[Teacher]]

  def bioUpdate(id: Long, bio: String): IO[Int]

  def payment(lesson: Lesson): IO[Int]

  def updateLessonStatus(lessonId: Long, teacherId: Long): IO[Int]

  def teacherGrade(teacherId: Long): IO[(Double, Int)]
}


