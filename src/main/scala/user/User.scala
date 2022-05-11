package user

import user.User.Insert


sealed trait UserType

case object Student extends UserType

case object Teacher extends UserType

case object Moderator extends UserType


sealed trait Sex

case object Female extends Sex

case object Male extends Sex


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