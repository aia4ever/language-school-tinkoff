package teacher

import cats.effect.IO
import data.dto.{Lesson, Teacher}
import doobie.util.transactor.Transactor.Aux
import lesson.LessonRepository
import doobie.implicits._
import user.UserRepository

import java.time.Instant
import java.time.temporal.ChronoUnit


class TeacherService(xa: Aux[IO, Unit])(tRep: TeacherRepository, lRep: LessonRepository, uRep: UserRepository) {

  def deleteLesson(lessonId: Long, teacherId: Long): IO[Int] = lRep.deleteLesson(lessonId, teacherId).transact(xa)

  def updateLesson(lesson: Lesson, teacherId: Long): IO[Lesson] = lRep.updateLesson(lesson, teacherId).transact(xa)

  def getLesson(lessonId: Long, teacherId: Long): IO[Lesson] = lRep.teacherLesson(lessonId, teacherId).transact(xa).map {
    case Some(lesson) => lesson
    case None => throw new Exception("You can only access your lessons")
  }

  def isTeacher(id: Long): IO[Boolean] = tRep.getTeacher(id).map(_.fold(false)(_ => true)).transact(xa)

  def upcomingLessons(teacherId: Long): IO[List[Lesson]] = lRep.upcomingLessonsByTeacher(teacherId).transact(xa)

  def newLesson(lesson: Lesson.Insert, teacherId: Long): IO[Lesson] =
    (
      if (teacherId == lesson.teacherId) for {
        list <- lRep.teacherLessonsByDate(teacherId, lesson.date)
        res <- if (list.isEmpty) lRep.createLesson(lesson)
        else throw new Exception("You have another lesson on this time")
      } yield res
      else throw new Exception("Only you can be the teacher in your lessons")).transact(xa)



  def insertOrUpdateBio(teacherId: Long, bio: String): IO[Teacher] =
    (for {
      update <- tRep.bioUpdate(teacherId, bio)
      res <- update match {
        case 1 => tRep.getTeacher(teacherId)
        case _ => throw new Exception("Something went wrong")
      }
    } yield res.get.toTeacher).transact(xa)


  def nextLesson(teacherId: Long): IO[Lesson] = lRep.upcomingLessonsByTeacher(teacherId).transact(xa)
    .map(_.headOption match {
      case Some(ls) => ls
      case None => throw new Exception("You don't have next lesson")
    })

  def previousLessons(teacherId: Long): IO[List[Lesson]] = lRep.previousLessonsByTeacher(teacherId).transact(xa)

  def withdrawal(teacherId: Long, amount: BigDecimal): IO[BigDecimal] =
    (for {
      balance <- uRep.balance(teacherId)
      res <-  if (balance._1 >= amount) uRep.withdrawal(teacherId, amount)
      else throw new Exception("Not enough money")
    } yield res).transact(xa)

  def findTeacher(teacherId: Long): IO[Teacher] = tRep.getTeacher(teacherId).transact(xa).map {
    case Some(teacher) => teacher.toTeacher
    case None => throw new Exception(s"Teacher with id:$teacherId not found")
  }

  def allTeachers(): IO[List[Teacher]] = tRep.getAllTeachers.map(_.map(_.toTeacher)).transact(xa)

  def payment(lessonId: Long, teacherId: Long): IO[Int] =
    (for {
      lessonOpt <- lRep.teacherLesson(lessonId, teacherId)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw new Exception("There is no lesson with this ID")
      }
      isPurchased = lesson.purchased
      studentId = lesson.studentId.fold(0L)(identity)
      date = lesson.date
     res <- if (studentId != 0 && !isPurchased && date.plus(1,ChronoUnit.HOURS).isBefore(Instant.now())) {
       tRep.updateLessonStatus(lessonId, teacherId)
       uRep.payment(lesson)
     } else throw new Exception("Payment can't be made")
    } yield res).transact(xa)

}