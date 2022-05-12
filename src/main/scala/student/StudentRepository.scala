package student

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._

trait StudentRepository {

  def isStudent(studentId: Long): ConnectionIO[Option[String]]

  def teacherGrade(teacherId: Long): ConnectionIO[Option[(Double, Int)]]

  def evaluateTeacherUpdate(teacherId: Long)(newGrade: Double, newAmount: Int): ConnectionIO[Int]

}


