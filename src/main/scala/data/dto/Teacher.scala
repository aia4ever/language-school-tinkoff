package data.dto

import data.dto.Teacher.Insert
import util.Sex


case class Teacher(
                  id: Long,
                  firstname: String,
                  surname: String,
                  sex: Sex,
                  bio: Option[String],
                  averageGrade: Option[Double],
                  gradeAmount: Option[Int]
                  ) {
  lazy val toInsert: Insert = Insert(id, firstname, surname, sex.toString, bio, averageGrade, gradeAmount)
}


object Teacher {
  case class Insert(
                            id: Long,
                            firstname: String,
                            surname: String,
                            sex: String,
                            bio: Option[String],
                            averageGrade: Option[Double],
                            gradeAmount: Option[Int]
                       )
}


