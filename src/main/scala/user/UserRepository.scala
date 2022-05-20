package user

import cats.effect.IO
import data.dao.UserDao
import data.dto.{Balance, User}
import data.req.LoginReq
import doobie.implicits._


trait UserRepository  {
  def createUser(insert: User.Insert): IO[UserDao]

  def getUserById(id: Long):  IO[Option[UserDao]]

  def deleteById(id: Long): IO[Int]

  def logout(session: String): IO[Int]

  def findByLoginNonBlocked(req: LoginReq): IO[Option[UserDao]]

  def login(user: User): IO[String]

  def cashIn(id: Long, amount: BigDecimal): IO[Balance]

  def balance(id: Long): IO[Balance]

  def withdrawal(userId: Long, amount: BigDecimal): IO[Balance]
}
