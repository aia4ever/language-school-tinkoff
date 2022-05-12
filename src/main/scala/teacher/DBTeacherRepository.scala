package teacher

import data.dao.TeacherDao
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._


class DBTeacherRepository extends TeacherRepository {
  def getTeacher(teacherId: Long): ConnectionIO[Option[TeacherDao]] =
    sql"""select ut.id, ut.firstname, ut.surname, ut.sex, te.bio, te.average_grade, te.grade_amount
      from user_table ut
          left join teacher_extension te on ut.id = te.teacher_id and ut.user_type = 'Teacher'
      where ut.id = $teacherId
       """.query[TeacherDao].option

  def getAllTeachers: ConnectionIO[List[TeacherDao]] =
    sql"""
      select ut.id, ut.firstname, ut.surname, ut.sex, te.bio, te.average_grade, te.grade_amount
      from user_table ut
          left join teacher_extension te on ut.id = te.teacher_id and ut.user_type = 'Teacher'
       """.query[TeacherDao].stream.compile.toList

  def bioUpdate(id: Long, bio: String): ConnectionIO[Int] = {
    sql"""insert into teacher_extension(teacher_id, bio) values ($id, $bio)
            on conflict (teacher_id) do update set bio = $bio
       """.update.run
  }

}