package util.dictionary

import data.dao.UserDao
import data.dto.{Balance, Lesson, User}
import doobie.free.connection.ConnectionIO
import doobie.implicits._

object UserDictionary {

  val cols = Seq("id", "login", "password", "firstname", "surname", "email", "phone_number", "sex", "user_type")

  val base = sql"select id, login, firstname, surname, password, email, phone_number, sex, user_type  from user_table"

  def deleteAcc(id: Long): ConnectionIO[Int] =
    sql"delete from user_table where id = $id"
      .update.run


  def create(insert: User.Insert): ConnectionIO[UserDao] = {
    sql"""
         insert into user_table (login, password, firstname, surname, email, phone_number, sex, user_type)
            values (${insert.login}, ${insert.password}, ${insert.firstname}, ${insert.surname}, ${insert.email}, ${insert.phoneNumber}, ${insert.sex}, ${insert.userType})
         """.update.withUniqueGeneratedKeys[UserDao](cols: _*)
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

  def cashIn(userId: Long, amount: BigDecimal): ConnectionIO[Balance] =
    sql"""
         update user_table
         set wallet = wallet + $amount
         where id = $userId
       """
      .update.withUniqueGeneratedKeys( "wallet", "reserved")

  def balance(id: Long): ConnectionIO[Balance] =
    sql"""
         select wallet, reserved from user_table
        where id = $id
       """
      .query[Balance].unique

  def withdrawal(userId: Long, amount: BigDecimal): ConnectionIO[Balance] =
    sql"""
         update user_table
          set wallet = $amount
           where id = $userId
       """.update.withUniqueGeneratedKeys("wallet", "reserved")

  def reserve(id: Long, amount: BigDecimal): ConnectionIO[Int] =
    sql"""
        update user_table
         set wallet = wallet - $amount,
             reserved = reserved + $amount
         where id = $id
       """.update.run

  def unreserve(id: Long, amount: BigDecimal): ConnectionIO[Int] =
    sql"""
        update user_table
         set wallet = wallet + $amount,
             reserved = reserved - $amount
         where id = $id
       """.update.run

  def payment(lesson: Lesson): ConnectionIO[Int] =
    sql"""
         update user_table
        set wallet = wallet + ${lesson.price}
        where id = ${lesson.teacherId};
        update user_table
        set reserved = reserved - ${lesson.price}
        where id = ${lesson.studentId}
       """
      .update.run
}
