package util.dictionary

import data.dao.TeacherDao
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._
import util.TeacherType


object TeacherDictionary {

  private val teacherType: String = TeacherType.toString

  def getTeacher(teacherId: Long): ConnectionIO[Option[TeacherDao]] =
    sql"""select ut.id, ut.firstname, ut.surname, ut.sex, te.bio, te.average_grade, te.grade_amount
        from user_table ut
        left join teacher_extension te on ut.id = te.teacher_id
        where ut.id = $teacherId and ut.user_type = $teacherType
   """.query[TeacherDao].option


  def getAllTeachers: ConnectionIO[List[TeacherDao]] =
    sql"""
      select ut.id, ut.firstname, ut.surname, ut.sex, te.bio, te.average_grade, te.grade_amount
      from user_table ut
          left join teacher_extension te on ut.id = te.teacher_id
            where ut.user_type = $teacherType
       """.query[TeacherDao].stream.compile.toList

  def teacherGrade(teacherId: Long): ConnectionIO[Option[(Double, Int)]] =
    sql"""
        select average_grade, grade_amount from teacher_extension
        where teacher_id = $teacherId
        for update skip locked
       """
      .query[(Double, Int)].option

  def bioUpdate(id: Long, bio: String): ConnectionIO[Int] =
    sql"""
        insert into teacher_extension(teacher_id, bio) values ($id, $bio)
        on conflict (teacher_id) do update set bio = $bio
       """.update.run

  def updateLessonStatus(lessonId: Long, teacherId: Long): ConnectionIO[Int] =
    sql"""
        update lesson
        set is_purchased = true
        where id = $lessonId and teacher_id = $teacherId
       """.update.run
}
