package teacher

import teacher.Teacher.TeacherInsert
import user.Sex


case class Teacher(
                  id: Long,
                  firstname: String,
                  surname: String,
                  sex: Sex,
                  bio: Option[String],
                  averageGrade: Option[Double],
                  gradeAmount: Option[Int]
                  ) {
  def toInsert: TeacherInsert = TeacherInsert(id, firstname, surname, sex.toString, bio, averageGrade, gradeAmount)
}


object Teacher {
  case class TeacherInsert(
                            id: Long,
                            firstname: String,
                            surname: String,
                            sex: String,
                            bio: Option[String],
                            averageGrade: Option[Double],
                            gradeAmount: Option[Int]
                       )
}


