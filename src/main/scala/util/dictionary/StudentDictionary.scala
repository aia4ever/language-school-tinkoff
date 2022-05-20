package util.dictionary

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._


object StudentDictionary {

  def studentUserType(studentId: Long): ConnectionIO[String] =
    sql"""
         select user_type from user_table
            where id = $studentId
       """.query[String].unique



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
