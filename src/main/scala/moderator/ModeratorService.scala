package moderator

import cats.effect.IO
import data.dto.{Lesson, User}
import session.SessionRepository
import util.ApiErrors.{AccessDeniedError, InvalidSessionError, LessonNotFoundError, UserNotFoundError}
import util.ModeratorType

class ModeratorService(moderatorRepository: ModeratorRepository, sessionRepository: SessionRepository) {

  def blockUser(session: String, userId: Long): IO[Boolean] =
    for {
      userIdOpt <- sessionRepository.getIdBySession(session)
      id = userIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
    isModerator <- moderatorRepository.userById(id).map{
      case Some(us) => us.toUser.userType == ModeratorType
      case None => false
    }
    isReqMod <- moderatorRepository.userById(userId).map{
      case Some(us) => us.toUser.userType == ModeratorType
      case None => false
    }
    res <- if (isModerator && !isReqMod) moderatorRepository.blockUser(userId).map(_ != 0)
    else IO.raiseError(AccessDeniedError)
  } yield res

  def unblock(session: String, userId: Long): IO[Boolean] =
    for {
      userIdOpt <- sessionRepository.getIdBySession(session)
      id = userIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isModerator <- moderatorRepository.userById(id).map{
        case Some(us) => us.toUser.userType == ModeratorType
        case None => false
      }
      res <- if (isModerator) moderatorRepository.unblockUser(userId).map(_ != 0)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def deleteUser(session: String, userId: Long): IO[Boolean] =
    for {
      userIdOpt <- sessionRepository.getIdBySession(session)
      id = userIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isModerator <- moderatorRepository.userById(id).map{
        case Some(us) => us.toUser.userType == ModeratorType
        case None => false
      }
      isReqMod <- moderatorRepository.userById(userId).map{
        case Some(us) => us.toUser.userType == ModeratorType
        case None => false
      }
      res <- if (isModerator && !isReqMod) moderatorRepository.deleteUser(userId).map(_ != 0)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def deleteLesson(session: String, lessonId: Long): IO[Boolean] =
    for {
      userIdOpt <- sessionRepository.getIdBySession(session)
      id = userIdOpt match {
        case Some(id) => id
        case None => throw InvalidSessionError
      }
      isModerator <- moderatorRepository.userById(id).map{
        case Some(us) => us.toUser.userType == ModeratorType
        case None => false
      }
      res <- if (isModerator) moderatorRepository.deleteLesson(lessonId).map(_ != 0)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def getUser(session: String, userId: Long): IO[User] = for {
    userIdOpt <- sessionRepository.getIdBySession(session)
    id = userIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    isModerator <- moderatorRepository.userById(id).map{
      case Some(us) => us.toUser.userType == ModeratorType
      case None => false
    }
    userOpt <- if (isModerator) moderatorRepository.userById(userId)
    else IO.raiseError(AccessDeniedError)
    res = userOpt match {
      case Some(us) => us.toUser
      case None => throw UserNotFoundError
    }
  } yield res

  def getLesson(session: String, lessonId: Long): IO[Lesson] = for {
    userIdOpt <- sessionRepository.getIdBySession(session)
    id = userIdOpt match {
      case Some(id) => id
      case None => throw InvalidSessionError
    }
    isModerator <- moderatorRepository.userById(id).map{
      case Some(us) => us.toUser.userType == ModeratorType
      case None => false
    }
    lessonOpt <- if (isModerator) moderatorRepository.lessonById(lessonId)
    else IO.raiseError(AccessDeniedError)
    res = lessonOpt match {
      case Some(ls) => ls
      case None => throw LessonNotFoundError
    }
  } yield res
}
