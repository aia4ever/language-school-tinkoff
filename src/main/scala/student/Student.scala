package student

import student.Student.Insert
import user.Sex

case class Student(
                    id: Long,
                    firstname: String,
                    surname: String,
                    sex: Sex
                  ) {
  def toInsert: Insert = Insert(id, firstname, surname, sex.toString)
}


object Student {
  case class Insert(
                            id: Long,
                            firstname: String,
                            surname: String,
                            sex: String
                          )
}