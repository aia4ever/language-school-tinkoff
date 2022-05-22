package data.dto

import data.dto.Student.Insert
import util.Sex

case class Student(
                    id: Long,
                    firstname: String,
                    surname: String,
                    sex: Sex
                  ) {
  lazy val toInsert: Insert = Insert(id, firstname, surname, sex.toString)
}


object Student {
  case class Insert(
                            id: Long,
                            firstname: String,
                            surname: String,
                            sex: String
                          )
}