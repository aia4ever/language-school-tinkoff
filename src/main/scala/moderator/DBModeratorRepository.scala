package moderator

import cats.effect.IO
import config.DI.ResTransactor
import data.dao.UserDao
import data.dto.Lesson
import doobie.implicits._
import util.Util.dbConnection
import util.dictionary.ModeratorDictionary

class DBModeratorRepository(xa: ResTransactor[IO]) extends ModeratorRepository {

  def blockUser(userId: Long): IO[Int] = ModeratorDictionary.closeAllSessions(userId)
    .flatMap(_ => ModeratorDictionary.blockUser(userId)).cast(xa)

  def closeAllSessions(userId: Long): IO[Int] = ModeratorDictionary.closeAllSessions(userId).cast(xa)

  def unblockUser(userId: Long): IO[Int] = ModeratorDictionary.unblockUser(userId).cast(xa)

  def deleteUser(userId: Long): IO[Int] = ModeratorDictionary.deleteUser(userId).cast(xa)

  def deleteLesson(lessonId: Long): IO[Int] = ModeratorDictionary.deleteLesson(lessonId).cast(xa)

  def lessonById(lessonId: Long): IO[Option[Lesson]] = ModeratorDictionary.lessonById(lessonId).cast(xa)

  def userById(userId: Long): IO[Option[UserDao]] = ModeratorDictionary.userById(userId).cast(xa)

}
