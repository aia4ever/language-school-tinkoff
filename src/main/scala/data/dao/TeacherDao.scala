package data.dao

import data.dto.Teacher

case class TeacherDao(
                       id: Long,
                       firstname: String,
                       surname: String,
                       sex: String,
                       bio: Option[String],
                       averageGrade: Option[Double],
                       gradeAmount: Option[Int]
                     ) {


  lazy val toTeacher: Teacher = Teacher(id, firstname, surname, sex, bio, averageGrade, gradeAmount)

}
