package moderator

import cats.effect.IO
import data.dto.{Lesson, User}
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors.{LessonNotFoundError, UserNotFoundError}
import util.dictionary.ModeratorDictionary

class DBModeratorRepository(xa: Aux[IO, Unit]) extends ModeratorRepository {

  def isMod(userId: Long): IO[Boolean] = ModeratorDictionary.userById(userId).transact(xa).map {
    case Some(user) => user.userType == "ModeratorType"
    case None => throw UserNotFoundError
  }

  def blockUser(userId: Long): IO[Boolean] = ModeratorDictionary.closeAllSessions(userId)
    .flatMap(_ => ModeratorDictionary.blockUser(userId)).transact(xa)

  def closeAllSessions(userId: Long): IO[Int] = ModeratorDictionary.closeAllSessions(userId).transact(xa)

  def unblockUser(userId: Long): IO[Boolean] = ModeratorDictionary.unblockUser(userId).transact(xa)

  def deleteUser(userId: Long): IO[Boolean] = ModeratorDictionary.deleteUser(userId).transact(xa)

  def deleteLesson(lessonId: Long): IO[Boolean] = ModeratorDictionary.deleteLesson(lessonId).transact(xa)

  def lessonById(lessonId: Long): IO[Lesson] = ModeratorDictionary.lessonById(lessonId).map {
    case Some(ls) => ls
    case None => throw LessonNotFoundError
  } .transact(xa)

  def userById(userId: Long): IO[User] = ModeratorDictionary.userById(userId).map {
    case Some(ls) => ls.toUser
    case None => throw UserNotFoundError
  } .transact(xa)

}
