package teacher

import cats.effect.IO
import config.DI.ResTransactor
import data.dao.TeacherDao
import data.dto.Lesson
import data.req.LessonUpdateReq
import doobie.implicits._
import util.ApiErrors._
import util.TeacherType
import util.Util.dbConnection
import util.dictionary.{LessonDictionary, TeacherDictionary, UserDictionary}

import java.time.Instant


class DBTeacherRepository(xa: ResTransactor[IO]) extends TeacherRepository {

  override def deleteLesson(lessonId: Long, teacherId: Long): IO[Int] =
    LessonDictionary.deleteLesson(lessonId, teacherId).cast(xa)

  override def updateLesson(lesson: LessonUpdateReq): IO[Lesson] =
    LessonDictionary.updateLesson(lesson).cast(xa)

  override def getLesson(lessonId: Long, teacherId: Long): IO[Option[Lesson]] =
    LessonDictionary.getLesson(lessonId, teacherId, TeacherType).cast(xa)


  override def upcomingLessons(teacherId: Long): IO[List[Lesson]] =
    LessonDictionary.upcomingLessons(teacherId, TeacherType).cast(xa)

  override def getLessonsByDate(userId: Long, date: Instant): IO[List[Lesson]] =
    LessonDictionary.getLessonsByDate(userId, TeacherType, date).cast(xa)

  override def newLesson(lesson: Lesson.Insert): IO[Lesson] =
    LessonDictionary.createLesson(lesson).cast(xa)

  override def bioUpdate(teacherId: Long, bio: String): IO[Int] =
    TeacherDictionary.bioUpdate(teacherId, bio).cast(xa)


  override def previousLessons(teacherId: Long): IO[List[Lesson]] =
    LessonDictionary.previousLessons(teacherId, TeacherType).cast(xa)

  override def getTeacher(teacherId: Long): IO[Option[TeacherDao]] =
    TeacherDictionary.getTeacher(teacherId).cast(xa)

  override def getAllTeachers: IO[List[TeacherDao]] =
    TeacherDictionary.getAllTeachers.cast(xa)

  override def updateLessonStatus(lessonId: Long, teacherId: Long): IO[Int] =
    TeacherDictionary.updateLessonStatus(lessonId, teacherId).cast(xa)

  override def payment(lesson: Lesson): IO[Int] = UserDictionary.payment(lesson).cast(xa)

  def teacherGrade(teacherId: Long): IO[Option[(Double, Int)]] =
    TeacherDictionary.teacherGrade(teacherId).cast(xa)
}