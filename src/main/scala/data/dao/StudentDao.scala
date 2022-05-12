package data.dao

import student.Student
import util.Implicits._

case class StudentDao(
                       id: Long,
                       firstname: String,
                       surname: String,
                       sex: String
                     ) {


 lazy val toStudent: Student = Student(id, firstname, surname, sex)

}
