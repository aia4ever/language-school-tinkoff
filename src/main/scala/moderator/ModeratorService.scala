package moderator

import cats.effect.IO
import data.dto.{Lesson, User}
import session.SessionRepository
import util.ApiErrors.AccessDeniedError

class ModeratorService(moderatorRepository: ModeratorRepository, sessionRepository: SessionRepository) {

  def blockUser(session: String, userId: Long): IO[Boolean] =
    for {
    id <- sessionRepository.getIdBySession(session)
    isModerator <- moderatorRepository.isMod(id)
    isReqMod <- moderatorRepository.isMod(userId)
    res <- if (isModerator && !isReqMod) moderatorRepository.blockUser(userId)
    else IO.raiseError(AccessDeniedError)
  } yield res

  def unblock(session: String, userId: Long): IO[Boolean] =
    for {
      id <- sessionRepository.getIdBySession(session)
      isModerator <- moderatorRepository.isMod(id)
      res <- if (isModerator) moderatorRepository.unblockUser(userId)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def deleteUser(session: String, userId: Long): IO[Boolean] =
    for {

      id <- sessionRepository.getIdBySession(session)
      isModerator <- moderatorRepository.isMod(id)
      isReqMod <- moderatorRepository.isMod(userId)
      res <- if (isModerator && !isReqMod) moderatorRepository.deleteUser(userId)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def deleteLesson(session: String, lessonId: Long): IO[Boolean] =
    for {

      id <- sessionRepository.getIdBySession(session)
      isModerator <- moderatorRepository.isMod(id)
      res <- if (isModerator) moderatorRepository.deleteLesson(lessonId)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def getUser(session: String, userId: Long): IO[User] = for {
    id <- sessionRepository.getIdBySession(session)
    isModerator <- moderatorRepository.isMod(id)
    res <- if (isModerator) moderatorRepository.userById(userId)
    else IO.raiseError(AccessDeniedError)
  } yield res

  def getLesson(session: String, lessonId: Long): IO[Lesson] = for {
    id <- sessionRepository.getIdBySession(session)
    isModerator <- moderatorRepository.isMod(id)
    res <- if (isModerator) moderatorRepository.lessonById(lessonId)
    else IO.raiseError(AccessDeniedError)
  } yield res

}
