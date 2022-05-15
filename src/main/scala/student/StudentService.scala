package student

import cats.effect.IO
import cats.effect.IO.asyncForIO
import data.dto.Lesson
import data.req.GradeReq
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import lesson.LessonRepository
import user.UserRepository


class StudentService(xa: Aux[IO, Unit])(stRep: StudentRepository, lRep: LessonRepository, uRep: UserRepository) {

  def isStudent(studentId: Long): IO[Boolean] =
    stRep.isStudent(studentId).transact(xa).map {
      case Some(ut) if ut == "Student" => true
      case _ => false
    }


  def cashIn(id: Long, amount: Double): IO[BigDecimal] = uRep.cashIn(id, amount).transact(xa)

  def evaluateTeacherUpdate(req: GradeReq): IO[Int] =
    (for {
      grade <- stRep.teacherGrade(req.teacherId).map(_.fold((0.0, 0))(identity))
      newGrade = (grade._1 * grade._2 + req.rate) / (grade._2 + 1)
      res <- stRep.evaluateTeacherUpdate(req.teacherId)(newGrade, grade._2 + 1)
    } yield res).transact(xa)


  def next(studentId: Long): IO[Lesson] =
    lRep.upcomingLessonsByStudent(studentId).transact(xa).map(_.headOption match {
      case Some(ls) => ls
      case None => throw new Exception("List of upcoming lessons is empty")
    })

  def previous(studentId: Long): IO[List[Lesson]] = lRep.previousLessonsByStudent(studentId).transact(xa)

  def upcoming(studentId: Long): IO[List[Lesson]] = lRep.upcomingLessonsByStudent(studentId).transact(xa)

  def signUp(lessonId: Long, studentId: Long): IO[Lesson] = (
    for {
      lessonOpt <- lRep.emptyLesson(lessonId)
      lesson = lessonOpt match {
        case Some(l) => l
        case None => throw new Exception("You can't access this lesson")
      }
      list <- lRep.studentLessonsByDate(studentId, lesson.date)
      balance <- uRep.balance(studentId)
      _ <- if (balance._1 > lesson.price) uRep.reserve(studentId, lesson.price)
      else throw new Exception("Not enough money")
      res <- if (list.isEmpty) lRep.signUp(lessonId, studentId)
             else throw new Exception("You have another lesson on this time")
    } yield res).transact(xa)


  def signOut(lessonId: Long, studentId: Long): IO[Int] =
    (for {
      lessonOpt <- lRep.studentLesson(lessonId, studentId)
      lesson = lessonOpt match {
        case Some(ls) => ls
        case None => throw new Exception("Something went wrong")
      }
      _ <- uRep.unreserve(lessonId, lesson.price)
      res <- lRep.signOut(lessonId, studentId)
    } yield res ).transact(xa)

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]] =
    lRep.lessonsByTeacher(teacherId).transact(xa)

  def lesson(lessonId: Long): IO[Lesson] = lRep.emptyLesson(lessonId).transact(xa).map {
    case Some(ls) => ls
    case None => throw new Exception("Something went wrong")
  }

  def yourLesson(lessonId: Long, studentId: Long): IO[Lesson] =
    lRep.studentLesson(lessonId, studentId).transact(xa).map {
      case Some(ls) => ls
      case None => throw new Exception("Something went wrong")
    }


}
