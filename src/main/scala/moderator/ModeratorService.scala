package moderator

import cats.effect.IO
import data.dto.{Lesson, User}
import org.http4s.Request
import session.SessionRepository
import util.ApiErrors.AccessDeniedError
import util.Util.auth

class ModeratorService(moderatorRepository: ModeratorRepository, sessionRepository: SessionRepository) {

  def blockUser(req: Request[IO], userId: Long): IO[Boolean] =
    for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    isModerator <- moderatorRepository.isMod(id)
    isReqMod <- moderatorRepository.isMod(userId)
    res <- if (isModerator && !isReqMod) moderatorRepository.blockUser(userId)
    else IO.raiseError(AccessDeniedError)
  } yield res

  def unblock(req: Request[IO], userId: Long): IO[Boolean] =
    for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      isModerator <- moderatorRepository.isMod(id)
      res <- if (isModerator) moderatorRepository.unblockUser(userId)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def deleteUser(req: Request[IO], userId: Long): IO[Boolean] =
    for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      isModerator <- moderatorRepository.isMod(id)
      isReqMod <- moderatorRepository.isMod(userId)
      res <- if (isModerator && !isReqMod) moderatorRepository.deleteUser(userId)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def deleteLesson(req: Request[IO], lessonId: Long): IO[Boolean] =
    for {
      session <- auth(req)
      id <- sessionRepository.getIdBySession(session)
      isModerator <- moderatorRepository.isMod(id)
      res <- if (isModerator) moderatorRepository.deleteLesson(lessonId)
      else IO.raiseError(AccessDeniedError)
    } yield res

  def getUser(req: Request[IO], userId: Long): IO[User] = for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    isModerator <- moderatorRepository.isMod(id)
    res <- if (isModerator) moderatorRepository.userById(userId)
    else IO.raiseError(AccessDeniedError)
  } yield res

  def getLesson(req: Request[IO], lessonId: Long): IO[Lesson] = for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    isModerator <- moderatorRepository.isMod(id)
    res <- if (isModerator) moderatorRepository.lessonById(lessonId)
    else IO.raiseError(AccessDeniedError)
  } yield res

}
