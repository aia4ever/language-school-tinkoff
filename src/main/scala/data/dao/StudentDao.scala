package data.dao

import data.dto.Student
import util.Implicits._

case class StudentDao(
                       id: Long,
                       firstname: String,
                       surname: String,
                       sex: String
                     ) {


 lazy val toStudent: Student = Student(id, firstname, surname, sex)

}
