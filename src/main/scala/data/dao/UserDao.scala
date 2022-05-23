package data.dao

import data.dto.User

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


  lazy val toUser: User = User(id, login, firstname, surname, password, email, phoneNumber, sex, userType)

}
