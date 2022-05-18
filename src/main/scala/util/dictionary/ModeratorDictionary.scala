package util.dictionary

import data.dao.UserDao
import data.dto.Lesson
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._

object ModeratorDictionary {

  def blockUser(userId: Long): ConnectionIO[Boolean] =
    sql"""
        update user_table
        set is_blocked = true
        where id = $userId
       """
      .update.run.map(_ != 0)


  def unblockUser(userId: Long): ConnectionIO[Boolean] =
    sql"""
         update user_table
        set is_blocked = false
        where id = $userId
       """
      .update.run.map(_ != 0)

  def deleteUser(userId: Long): ConnectionIO[Boolean] =
    sql"""
         delete from user_table
        where id = $userId
       """
      .update.run.map(_ != 0)

  def deleteLesson(lessonId: Long): ConnectionIO[Boolean] =
    sql"""
         delete from lesson
        where id = $lessonId
       """
      .update.run.map(_ != 0)

  def lessonById(lessonId: Long): ConnectionIO[Option[Lesson]] =
    sql"""
        select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark, is_purchased from lesson
        where id = $lessonId
       """
      .query[Lesson].option

  def userById(userId: Long): ConnectionIO[Option[UserDao]] =
    sql"""
         select id, login, firstname, surname, password, email, phone_number, sex, user_type  from user_table
        where id = $userId
        """
      .query[UserDao].option


  def closeAllSessions(userId: Long): ConnectionIO[Int] =
    sql"delete from session where id = $userId".update.run
}
