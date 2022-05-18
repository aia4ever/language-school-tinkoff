package user

import cats.effect.IO
import data.dto.User
import data.req.LoginReq
import org.http4s.Request
import session.SessionRepository
import util.ApiErrors.{InvalidLoginPasswordError, SomethingWentWrongError}
import util.Util.auth

class UserService(userRepository: UserRepository, sessionRepository: SessionRepository) {

  def create(insert: User.Insert): IO[User] = userRepository.createUser(insert)

  def login(req: LoginReq): IO[String] = for {
    user <- userRepository.findByLoginNonBlocked(req)
    res  <-  if (user.password == req.password) userRepository.login(user)
    else throw InvalidLoginPasswordError
  } yield res

  def logout(req: Request[IO]): IO[Unit] = for {
    session <- auth(req)
    res <- userRepository.logout(session)
  } yield res

  def delete(req: Request[IO], id: Long): IO[Int] =
    for {
    session <- auth(req)
    userId <- sessionRepository.getIdBySession(session)
    res <- if (userId == id )userRepository.deleteById(userId)
    else throw SomethingWentWrongError
    } yield res

  def getUser(req: Request[IO]): IO[User] = for {
    session <- auth(req)
    id <- sessionRepository.getIdBySession(session)
    res <- userRepository.getUserById(id)
  } yield res


}
