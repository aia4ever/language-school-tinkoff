package teacher

import data.dao.TeacherDao
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._

trait TeacherRepository {

  def getTeacher(teacherId: Long): ConnectionIO[Option[TeacherDao]]

  def getAllTeachers: ConnectionIO[List[TeacherDao]]

  def bioUpdate(id: Long, bio: String): ConnectionIO[Int]

}


