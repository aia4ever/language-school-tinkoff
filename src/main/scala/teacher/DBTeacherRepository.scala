package teacher

import cats.effect.IO
import data.dao.TeacherDao
import data.dto.Lesson
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors._
import util.TeacherType
import util.dictionary.{LessonDictionary, TeacherDictionary, UserDictionary}

import java.time.Instant


class DBTeacherRepository(xa: Aux[IO, Unit]) extends TeacherRepository {

  override def deleteLesson(lessonId: Long, teacherId: Long): IO[Int] =
    LessonDictionary.deleteLesson(lessonId, teacherId).transact(xa)

  override def updateLesson(lesson: Lesson, teacherId: Long): IO[Lesson] =
    LessonDictionary.updateLesson(lesson, teacherId).transact(xa)

  override def getLesson(lessonId: Long, teacherId: Long): IO[Option[Lesson]] =
    LessonDictionary.getLesson(lessonId, teacherId, TeacherType).transact(xa)



  override def upcomingLessons(teacherId: Long): IO[List[Lesson]] =
    LessonDictionary.upcomingLessons(teacherId, TeacherType).transact(xa)

  override def getLessonsByDate(userId: Long, date: Instant): IO[List[Lesson]] =
    LessonDictionary.getLessonsByDate(userId, TeacherType, date).transact(xa)

  override def newLesson(lesson: Lesson.Insert): IO[Lesson] =
    LessonDictionary.createLesson(lesson).transact(xa)

  override def bioUpdate(teacherId: Long, bio: String): IO[Int] =
    TeacherDictionary.bioUpdate(teacherId, bio).transact(xa)


  override def previousLessons(teacherId: Long): IO[List[Lesson]] =
    LessonDictionary.previousLessons(teacherId, TeacherType).transact(xa)

  override def getTeacher(teacherId: Long): IO[Option[TeacherDao]] =
    TeacherDictionary.getTeacher(teacherId).transact(xa)

  override def getAllTeachers: IO[List[TeacherDao]] =
    TeacherDictionary.getAllTeachers.transact(xa)

  override def updateLessonStatus(lessonId: Long, teacherId: Long): IO[Int] =
    TeacherDictionary.updateLessonStatus(lessonId, teacherId).transact(xa)

  override def payment(lesson: Lesson): IO[Int] = UserDictionary.payment(lesson).transact(xa)

  def teacherGrade(teacherId: Long): IO[Option[(Double, Int)]] =
    TeacherDictionary.teacherGrade(teacherId).transact(xa)
}