package teacher

import cats.effect.IO
import data.dto.{Lesson, Teacher}
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors._
import util.TeacherType
import util.dictionary.{LessonDictionary, TeacherDictionary, UserDictionary}

import java.time.Instant


class DBTeacherRepository(xa: Aux[IO, Unit]) extends TeacherRepository {

  override def deleteLesson(lessonId: Long, teacherId: Long): IO[Int] = LessonDictionary.deleteLesson(lessonId, teacherId).transact(xa)

  override def updateLesson(lesson: Lesson, teacherId: Long): IO[Lesson] = LessonDictionary.updateLesson(lesson, teacherId).transact(xa)

  override def getLesson(lessonId: Long, teacherId: Long): IO[Lesson] = LessonDictionary.getLesson(lessonId, teacherId, TeacherType).transact(xa).map {
    case Some(lesson) => lesson
    case None => throw AccessDeniedError
  }

  override def isTeacher(id: Long): IO[Boolean] = TeacherDictionary.getTeacher(id).map(_.fold(false)(_ => true)).transact(xa)

  override def upcomingLessons(teacherId: Long): IO[List[Lesson]] = LessonDictionary.upcomingLessons(teacherId, TeacherType).transact(xa)

  override def isNotBusy(userId: Long, date: Instant): IO[Boolean] =
    LessonDictionary.getLessonsByDate(userId, TeacherType, date).transact(xa).map(_.isEmpty)

  override def newLesson(lesson: Lesson.Insert): IO[Lesson] =
    LessonDictionary.createLesson(lesson).transact(xa)

  override def bioUpdate(teacherId: Long, bio: String): IO[Int] =
    TeacherDictionary.bioUpdate(teacherId, bio).transact(xa)



  override def nextLesson(teacherId: Long): IO[Lesson] = LessonDictionary.upcomingLessons(teacherId, TeacherType).transact(xa)
    .map(_.headOption match {
      case Some(ls) => ls
      case None => throw NoLessonError
    })

  override def previousLessons(teacherId: Long): IO[List[Lesson]] = LessonDictionary.previousLessons(teacherId, TeacherType).transact(xa)

  override def getTeacher(teacherId: Long): IO[Teacher] = TeacherDictionary.getTeacher(teacherId).transact(xa).map {
    case Some(teacher) => teacher.toTeacher
    case None => throw UserNotFoundError
  }

  override def getAllTeachers: IO[List[Teacher]] = TeacherDictionary.getAllTeachers.map(_.map(_.toTeacher)).transact(xa)

  override def updateLessonStatus(lessonId: Long, teacherId: Long): IO[Int] =
    TeacherDictionary.updateLessonStatus(lessonId, teacherId).transact(xa)

  override def payment(lesson: Lesson): IO[Int] = UserDictionary.payment(lesson).transact(xa)

  def teacherGrade(teacherId: Long): IO[(Double, Int)] =
    TeacherDictionary.teacherGrade(teacherId).transact(xa).map(_.fold((0.0, 0))(identity))
}