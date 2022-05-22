package user

import cats.effect.IO
import data.dto.User
import data.req.LoginReq
import session.SessionRepository
import util.ApiErrors.{InvalidLoginPasswordError, InvalidSessionError, SomethingWentWrongError, UserNotFoundError}


class UserService(userRepository: UserRepository, sessionRepository: SessionRepository) {

  def userId(session: String): IO[Long] = sessionRepository.getIdBySession(session).map {
    case Some(id) => id
    case None => throw InvalidSessionError
  }

  def create(insert: User.Insert): IO[User] = userRepository.createUser(insert).map(_.toUser)

  def login(req: LoginReq): IO[String] = for {
    userOpt <- userRepository.findByLoginNonBlocked(req)
    user = userOpt match {
      case Some(us) => us.toUser
      case None => throw InvalidLoginPasswordError
    }
    res <- if (user.password == req.password) userRepository.login(user)
    else throw InvalidLoginPasswordError
  } yield res

  def logout(session: String): IO[Unit] =
    userRepository.logout(session).map(_ => ())


  def delete(session: String, id: Long): IO[Int] =
    for {
      userId <- userId(session)
      res <- if (userId == id) userRepository.deleteById(userId)
      else throw SomethingWentWrongError
    } yield res

  def getUser(session: String): IO[User] = for {
    userId <- userId(session)
    userOpt <- userRepository.getUserById(userId)
    res = userOpt match {
      case Some(us) => us.toUser
      case None => throw UserNotFoundError
    }
  } yield res
}
