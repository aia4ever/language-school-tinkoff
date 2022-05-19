package user

import cats.effect.IO
import data.dto.User
import data.req.LoginReq
import session.SessionRepository
import util.ApiErrors.{InvalidLoginPasswordError, SomethingWentWrongError}

class UserService(userRepository: UserRepository, sessionRepository: SessionRepository) {

  def create(insert: User.Insert): IO[User] = userRepository.createUser(insert)

  def login(req: LoginReq): IO[String] = for {
    user <- userRepository.findByLoginNonBlocked(req)
    res  <-  if (user.password == req.password) userRepository.login(user)
    else throw InvalidLoginPasswordError
  } yield res

  def logout(session: String): IO[Unit] =
    userRepository.logout(session)


  def delete(session: String, id: Long): IO[Int] =
    for {
    userId <- sessionRepository.getIdBySession(session)
    res <- if (userId == id )userRepository.deleteById(userId)
    else throw SomethingWentWrongError
    } yield res

  def getUser(session: String): IO[User] = for {
    id <- sessionRepository.getIdBySession(session)
    res <- userRepository.getUserById(id)
  } yield res


}
