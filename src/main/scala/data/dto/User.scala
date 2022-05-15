package data.dto

import data.dto.User.Insert
import util.{Sex, UserType}




case class User(
                 id: Long,
                 login: String,
                 firstname: String,
                 surname: String,
                 password: String,
                 email: String,
                 phoneNumber: String,
                 sex : Sex,
                 userType: UserType
               ) {
  def toInsert: Insert = Insert(login, firstname, surname, password, email, phoneNumber, sex.toString, userType.toString)
}


object User {
  case class Insert(
                     login: String,
                     firstname: String,
                     surname: String,
                     password: String,
                     email: String,
                     phoneNumber: String,
                     sex : String,
                     userType: String
                   )
}