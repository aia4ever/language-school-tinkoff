package moderator

import cats.effect.IO
import data.dto.{Lesson, User}
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

class ModeratorService(xa: Aux[IO, Unit])(mRep: ModeratorRepository) {

  def isMod(userId: Long): IO[Boolean] = mRep.isMod(userId).transact(xa)

  def blockUser(userId: Long): IO[Boolean] = mRep.closeAllSessions(userId).flatMap(_ => mRep.blockUser(userId)).transact(xa)

  def closeAllSessions(userId: Long): IO[Int] = mRep.closeAllSessions(userId).transact(xa)

  def unblockUser(userId: Long): IO[Boolean] = mRep.unblockUser(userId).transact(xa)

  def deleteUser(userId: Long): IO[Boolean] = mRep.deleteUser(userId).transact(xa)

  def deleteLesson(userId: Long): IO[Boolean] = mRep.deleteLesson(userId).transact(xa)

  def lessonById(lessonId: Long): IO[Lesson] = mRep.lessonById(lessonId).map {
    case Some(ls) => ls
    case None =>throw new Exception("No lesson with this id")
  } .transact(xa)

  def userById(userId: Long): IO[User] = mRep.userById(userId).map {
    case Some(ls) => ls.toUser
    case None => throw new Exception("No user with this id")
  } .transact(xa)

}
