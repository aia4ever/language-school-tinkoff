package student

import cats.effect.IO
import data.req.GradeReq
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import lesson.Lesson


class StudentService(xa: Aux[IO, Unit]) {

  def isStudent(studentId: Long): IO[Boolean] =
    StudentRepository.isStudent(studentId).transact(xa).map {
      case Some(ut) if ut == "Student" => true
      case _ => false
    }



  def cashIn(amount: Double): IO[Double] = ???

  def evaluateTeacherUpdate(req: GradeReq): IO[Int] =
    (for {
      grade <- StudentRepository.teacherGrade(req.teacherId).map(_.fold((0.0, 0))(identity))
      newGrade = (grade._1 * grade._2 + req.rate) / (grade._2 + 1)
      res <- StudentRepository.evaluateTeacherUpdate(req.teacherId)(newGrade, grade._2 + 1)
    } yield res).transact(xa)


  def next(studentId: Long): IO[Lesson] =
    StudentRepository.upcomingLessons(studentId).transact(xa).map(_.headOption match {
      case Some(ls) => ls
      case None => throw new Exception("List of upcoming lessons is empty")
    })

  def previous(studentId: Long): IO[List[Lesson]] = StudentRepository.previousLessons(studentId).transact(xa)

  def upcoming(studentId: Long): IO[List[Lesson]] = StudentRepository.upcomingLessons(studentId).transact(xa)

  def signUp(lessonId: Long, studentId: Long): IO[Lesson] =
    StudentRepository.signUp(lessonId, studentId).transact(xa)

  def signOut(lessonId: Long, studentId: Long): IO[Int] =
    StudentRepository.signOut(lessonId, studentId).transact(xa)

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]] =
    StudentRepository.lessonsByTeacher(teacherId).transact(xa)

  def lesson(lessonId: Long): IO[Lesson] = StudentRepository.emptyLesson(lessonId).transact(xa).map {
    case Some(ls) => ls
    case None => throw new Exception("Something went wrong")
  }

  def yourLesson(lessonId: Long, studentId: Long): IO[Lesson] =
    StudentRepository.yourLesson(lessonId, studentId).transact(xa).map {
      case Some(ls) => ls
      case None => throw new Exception("Something went wrong")
    }


//  def grades(teacherId: Long): IO[(Double, Int)] =
//    StudentRepository.teacherGrade(teacherId).transact(xa).map(_.fold((0.0, 0))(identity))
}
