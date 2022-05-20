package moderator

import cats.effect.IO
import data.dao.UserDao
import data.dto.Lesson
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.dictionary.ModeratorDictionary

class DBModeratorRepository(xa: Aux[IO, Unit]) extends ModeratorRepository {

  def blockUser(userId: Long): IO[Int] = ModeratorDictionary.closeAllSessions(userId)
    .flatMap(_ => ModeratorDictionary.blockUser(userId)).transact(xa)

  def closeAllSessions(userId: Long): IO[Int] = ModeratorDictionary.closeAllSessions(userId).transact(xa)

  def unblockUser(userId: Long): IO[Int] = ModeratorDictionary.unblockUser(userId).transact(xa)

  def deleteUser(userId: Long): IO[Int] = ModeratorDictionary.deleteUser(userId).transact(xa)

  def deleteLesson(lessonId: Long): IO[Int] = ModeratorDictionary.deleteLesson(lessonId).transact(xa)

  def lessonById(lessonId: Long): IO[Option[Lesson]] = ModeratorDictionary.lessonById(lessonId).transact(xa)

  def userById(userId: Long): IO[Option[UserDao]] = ModeratorDictionary.userById(userId).transact(xa)

}
