package data.dao

import data.dto
import data.dto.User
import util.Implicits._

case class UserDao(
                    id: Long,
                    login: String,
                    firstname: String,
                    surname: String,
                    password: String,
                    email: String,
                    phoneNumber: String,
                    sex: String,
                    userType: String
                  ) {


  lazy val toUser: User = dto.User(id, login, firstname, surname, password, email, phoneNumber, sex, userType)

}
