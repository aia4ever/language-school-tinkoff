package data.dao

import data.dto.Student

case class StudentDao(
                       id: Long,
                       firstname: String,
                       surname: String,
                       sex: String
                     ) {


 lazy val toStudent: Student = Student(id, firstname, surname, sex)

}
