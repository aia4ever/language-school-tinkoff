package data.dao

import user.User
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


  def toUser: User = User(id, login, firstname, surname, password, email, phoneNumber, sex, userType)

}
