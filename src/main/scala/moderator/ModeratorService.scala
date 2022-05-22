package moderator

import cats.effect.IO
import data.dto.{Lesson, User}
import session.SessionRepository
import util.ApiErrors.{AccessDeniedError, InvalidSessionError, LessonNotFoundError, UserNotFoundError}
import util.ModeratorType

class ModeratorService(moderatorRepository: ModeratorRepository, sessionRepository: SessionRepository) {

  def id(session: String): IO[Long] = sessionRepository.getIdBySession(session).map {
    case Some(id) => id
    case None => throw InvalidSessionError
  }

  def isMod(id: Long): IO[Boolean] = moderatorRepository.userById(id).map {
    case Some(us) => us.toUser.userType == ModeratorType
    case None => false
  }

  def blockUser(session: String, userId: Long): IO[Boolean] =
    for {
      id <- id(session)
      isModerator <- isMod(id)
      isReqMod <- isMod(userId)
      res <- if (isModerator && !isReqMod) moderatorRepository.blockUser(userId).map(_!=0)
      else throw (AccessDeniedError)
    } yield res

  def unblock(session: String, userId: Long): IO[Boolean] =
    for {
      id <- id(session)
      isModerator <- isMod(id)
      res <- if (isModerator) moderatorRepository.unblockUser(userId).map(_!=0)
      else throw (AccessDeniedError)
    } yield res

  def deleteUser(session: String, userId: Long): IO[Boolean] =
    for {
      id <- id(session)
      isModerator <- isMod(id)
      isReqMod <- isMod(userId)
      res <- if (isModerator && !isReqMod) moderatorRepository.deleteUser(userId).map(_ != 0)
      else throw (AccessDeniedError)
    } yield res

  def deleteLesson(session: String, lessonId: Long): IO[Boolean] =
    for {
      id <- id(session)
      isModerator <- isMod(id)
      res <- if (isModerator) moderatorRepository.deleteLesson(lessonId).map(_ != 0)
      else throw (AccessDeniedError)
    } yield res

  def getUser(session: String, userId: Long): IO[User] = for {
    id <- id(session)
    isModerator <- isMod(id)
    userOpt <- if (isModerator) moderatorRepository.userById(userId)
    else throw (AccessDeniedError)
    res = userOpt match {
      case Some(us) => us.toUser
      case None => throw UserNotFoundError
    }
  } yield res

  def getLesson(session: String, lessonId: Long): IO[Lesson] = for {
    id <- id(session)
    isModerator <- isMod(id)
    lessonOpt <- if (isModerator) moderatorRepository.lessonById(lessonId)
    else throw (AccessDeniedError)
    res = lessonOpt match {
      case Some(ls) => ls
      case None => throw LessonNotFoundError
    }
  } yield res
}
