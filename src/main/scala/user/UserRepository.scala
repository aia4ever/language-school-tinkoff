package user

import data.dao.UserDao
import doobie.free.connection.ConnectionIO
import doobie.implicits._


trait UserRepository {

  def deleteAcc(id: Long): ConnectionIO[Int]

  def create(insert: User.Insert): ConnectionIO[UserDao]

  def findById(id: Long): ConnectionIO[Option[UserDao]]

  def findByLogin(login: String): ConnectionIO[Option[UserDao]]

  def deleteById(id: Long): ConnectionIO[Int]

  def createSession(id: Long, session: String): ConnectionIO[String]

  def logout(session: String): ConnectionIO[Int]

}
