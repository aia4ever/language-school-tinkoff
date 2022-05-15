package user

import data.dao.UserDao
import data.dto.User
import doobie.free.connection.ConnectionIO
import doobie.implicits._


trait UserRepository {

  def deleteAcc(id: Long): ConnectionIO[Int]

  def create(insert: User.Insert): ConnectionIO[UserDao]

  def findById(id: Long): ConnectionIO[Option[UserDao]]

  def findByLoginNonBlocked(login: String): ConnectionIO[Option[UserDao]]

  def deleteById(id: Long): ConnectionIO[Int]

  def createSession(id: Long, session: String): ConnectionIO[String]

  def logout(session: String): ConnectionIO[Int]

  def cashIn(id: Long, amount: BigDecimal): ConnectionIO[BigDecimal]

  def balance(id: Long): ConnectionIO[(BigDecimal, BigDecimal)]

  def withdrawal(userId: Long, amount: BigDecimal): ConnectionIO[BigDecimal]

  def reserve(id: Long, amount: BigDecimal) : ConnectionIO[Int]

  def unreserve(id: Long, amount: BigDecimal) : ConnectionIO[Int]

}
