package user

import data.dao.UserDao
import data.dto.User
import doobie.free.connection.ConnectionIO
import doobie.implicits._

class DBUserRepository extends UserRepository {

  val cols = Seq("id", "login", "password", "firstname", "surname", "email", "phone_number", "sex", "user_type")

  val base = sql"select id, login, firstname, surname, password, email, phone_number, sex, user_type  from user_table"

  def deleteAcc(id: Long): ConnectionIO[Int] =
    sql"delete from user_table where id = $id"
      .update.run


  def create(insert: User.Insert): ConnectionIO[UserDao] = {
    sql"""
         insert into user_table (login, password, firstname, surname, email, phone_number, sex, user_type)
            values (${insert.login}, ${insert.password}, ${insert.firstname}, ${insert.surname}, ${insert.email}, ${insert.phoneNumber}, ${insert.sex}, ${insert.userType})
         """.update.withUniqueGeneratedKeys[UserDao](cols:_*)
  }


  def findById(id: Long): ConnectionIO[Option[UserDao]] =
    (base ++ fr" where id = $id").query[UserDao].option

  def findByLoginNonBlocked(login: String): ConnectionIO[Option[UserDao]] =
    (base ++ fr" where login = $login and not is_blocked").query[UserDao].option

  def deleteById(id: Long): ConnectionIO[Int] =
    sql"""
         delete from user_table where id = $id
       """.update.run

  def createSession(id: Long, session: String): ConnectionIO[String] =
    sql"insert into session(user_id, session) values ($id, $session)".update.withUniqueGeneratedKeys[String]("session")

  def logout(session: String): ConnectionIO[Int] =
    sql"delete from session where session = $session".update.run

  override def cashIn(userId: Long, amount: BigDecimal): ConnectionIO[BigDecimal] =
    sql"""
         update user_table
         set wallet = (select wallet from user_table where id = $userId) + $amount
         where id = $userId
       """
      .update.withUniqueGeneratedKeys("id", "wallet")

  def balance(id: Long): ConnectionIO[(BigDecimal, BigDecimal)] =
    sql"""
         select wallet, reserved from user_table
        where id = $id
       """
      .query[(BigDecimal, BigDecimal)].unique

  def withdrawal(userId: Long, amount: BigDecimal): ConnectionIO[BigDecimal] =
    sql"""
         update user_table
          set wallet = $amount
           where id = $userId
       """.update.withUniqueGeneratedKeys("wallet")

  def reserve(id: Long, amount: BigDecimal) : ConnectionIO[Int] =
    sql"""
        update user_table
         set wallet = wallet - $amount,
             reserved = reserved + $amount
         where id = $id
       """.update.run

  def unreserve(id: Long, amount: BigDecimal) : ConnectionIO[Int] =
    sql"""
        update user_table
         set wallet = wallet + $amount,
             reserved = reserved - $amount
         where id = $id
       """.update.run
}