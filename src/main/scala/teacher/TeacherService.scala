package teacher

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import lesson.Lesson
import util.Implicits.cast

class TeacherService(implicit val xa: Aux[IO, Unit]) {

  def deleteLesson(lessonId: Long, teacherId: Long): IO[Int] = TeacherRepository.deleteLesson(lessonId, teacherId)

  def updateLesson(lesson: Lesson, teacherId: Long): IO[Lesson] = TeacherRepository.updateLesson(lesson, teacherId)

  def getLesson(lessonId: Long, teacherId: Long): IO[Lesson] = TeacherRepository.getLesson(lessonId, teacherId).map {
    case Some(lesson) => lesson
    case None => throw new Exception("You can only access your lessons")
  }

  def isTeacher(id: Long): IO[Boolean] = TeacherRepository.getTeacher(id).map(_.fold(false)(_ => true))

  def upcomingLessons(teacherId: Long): IO[List[Lesson]] = TeacherRepository.upcomingLessons(teacherId)

  def newLesson(lesson: Lesson.Insert, teacherId: Long): IO[Lesson] =
    if (teacherId == lesson.teacherId) TeacherRepository.createLesson(lesson)
    else throw new Exception("Only you can be the teacher in your lessons")


  def insertOrUpdateBio(teacherId: Long, bio: String): IO[Teacher] =
    for {
      update <- TeacherRepository.bioUpdate(teacherId, bio)
      res <- update match {
        case 1 => TeacherRepository.getTeacher(teacherId)
        case _ => throw new Exception("Something went wrong")
      }
    } yield res.get.toTeacher


  def nextLesson(teacherId: Long): IO[Lesson] = TeacherRepository.upcomingLessons(teacherId)
    .map(_.headOption match {
      case Some(ls) => ls
      case None => throw new Exception("You don't have next lesson")
    }
    )

  def previousLessons(teacherId: Long): IO[List[Lesson]] = TeacherRepository.previousLessons(teacherId)

  def withdrawal(teacherId: Long, amount: Double): IO[Double] = ???

  def findTeacher(teacherId: Long): IO[Teacher] = cast(TeacherRepository.getTeacher(teacherId)).map {
    case Some(teacher) => teacher.toTeacher
    case None => throw new Exception(s"Teacher with id:$teacherId not found")
  }

  def allTeachers(): IO[List[Teacher]] = TeacherRepository.getAllTeachers.map(_.map(_.toTeacher))

}