package user

import data.dao.UserDao
import doobie.free.connection.ConnectionIO
import doobie.implicits._


object UserRepository {

  val base = sql"select id, login, firstname, surname, password, email, phone_number, sex, user_type  from user_table"

  def deleteAcc(id: Long): ConnectionIO[Int] =
    sql"delete from user_table where id = $id"
      .update.run


  def create(insert: User.Insert): ConnectionIO[UserDao] = {
    sql"""
         insert into user_table (login, password, firstname, surname, email, phone_number, sex, user_type)
            values (${insert.login}, ${insert.password}, ${insert.firstname}, ${insert.surname}, ${insert.email}, ${insert.phoneNumber}, ${insert.sex}, ${insert.userType})
         """.update.withUniqueGeneratedKeys[UserDao]("id", "login", "password", "firstname", "surname", "email", "phone_number","sex", "user_type")
  }


  def findById(id: Long): ConnectionIO[Option[UserDao]] =
    (base ++ fr" where id = $id").query[UserDao].option

  def findByLogin(login: String): ConnectionIO[Option[UserDao]] =
    (base ++ fr" where login = $login").query[UserDao].option

  def deleteById(id: Long): ConnectionIO[Int] =
    sql"""
         delete from user_table where id = $id
       """.update.run

  def createSession(id: Long, session: String): ConnectionIO[String] =
    sql"insert into session(user_id, session) values ($id, $session)".update.withUniqueGeneratedKeys[String]("session")

  def logout(session: String): ConnectionIO[Int] =
    sql"delete from session where session = $session".update.run
}
