package student

import cats.effect.IO
import data.req.GradeReq
import doobie.util.transactor.Transactor.Aux
import lesson.Lesson
import util.Implicits.cast

class StudentService(implicit xa: Aux[IO, Unit]) {

  def isStudent(studentId: Long): IO[Boolean] =
    StudentRepository.isStudent(studentId).map {
      case Some(ut) if ut == "Student" => true
      case _ => false
    }



  def cashIn(amount: Double): IO[Double] = ???

  def evaluateTeacherUpdate(req: GradeReq): IO[Int] =
    for {
      grade <- StudentRepository.teacherGrade(req.teacherId).map(_.fold((0.0, 0))(identity))
      newGrade = (grade._1 * grade._2 + req.rate) / (grade._2 + 1)
      res <- StudentRepository.evaluateTeacherUpdate(req.teacherId)(newGrade, grade._2 + 1)
    } yield res


  def next(studentId: Long): IO[Lesson] =
    StudentRepository.upcomingLessons(studentId).map(_.headOption match {
      case Some(ls) => ls
      case None => throw new Exception("List of upcoming lessons is empty")
    })

  def previous(studentId: Long): IO[List[Lesson]] = StudentRepository.previousLessons(studentId)

  def upcoming(studentId: Long): IO[List[Lesson]] = StudentRepository.upcomingLessons(studentId)

  def signUp(lessonId: Long, studentId: Long): IO[Lesson] =
    StudentRepository.signUp(lessonId, studentId)

  def signOut(lessonId: Long, studentId: Long): IO[Int] =
    StudentRepository.signOut(lessonId, studentId)

  def lessonsByTeacher(teacherId: Long): IO[List[Lesson]] =
    StudentRepository.lessonsByTeacher(teacherId)

  def lesson(lessonId: Long): IO[Lesson] = StudentRepository.emptyLesson(lessonId).map {
    case Some(ls) => ls
    case None => throw new Exception("Something went wrong")
  }

  def yourLesson(lessonId: Long, studentId: Long): IO[Lesson] =
    StudentRepository.yourLesson(lessonId, studentId).map {
      case Some(ls) => ls
      case None => throw new Exception("Something went wrong")
    }


  def grades(teacherId: Long): IO[(Double, Int)] =
    StudentRepository.teacherGrade(teacherId).map(_.fold((0.0, 0))(identity))
}
