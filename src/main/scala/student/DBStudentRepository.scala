package student

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._

class DBStudentRepository extends StudentRepository {

  def isStudent(studentId: Long): ConnectionIO[Option[String]] =
    sql"""
         select user_type from user_table
            where id = $studentId
       """.query[String].option

  def teacherGrade(teacherId: Long): ConnectionIO[Option[(Double, Int)]] =
    sql"""
        select average_grade, grade_amount from teacher_extension
        where teacher_id = $teacherId
       """
      .query[(Double, Int)].option

  def evaluateTeacherUpdate(teacherId: Long)(newGrade: Double, newAmount: Int): ConnectionIO[Int] =
    sql"""
        insert into teacher_extension (teacher_id, average_grade, grade_amount)
        values ($teacherId, $newGrade, $newAmount)
        on conflict (teacher_id) do update
        set average_grade = $newGrade,
            grade_amount = $newAmount
         """
      .update.run
}