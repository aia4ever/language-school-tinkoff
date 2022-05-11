package user

import cats.effect.IO
import data.req._
import doobie.free.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import session.SessionRepository

import java.util.UUID

class UserService(xa: Aux[IO, Unit]) {

  def userNotFound = throw new Exception("User not found")

  def logout(session: String): IO[Unit] = UserRepository.logout(session).transact(xa).map {
    case 1 => ()
    case 0 => throw new Exception("Invalid session")
  }

  def byLogin(login: String): IO[User] = UserRepository.findByLogin(login).transact(xa).map {
    case Some(us) => us.toUser
    case None => userNotFound
  }
  def byId(id: Long): IO[User] = UserRepository.findById(id).transact(xa).map {
    case Some(us) => us.toUser
    case None => userNotFound
  }


  def create(insert: User.Insert): IO[User] = UserRepository.create(insert).transact(xa).map(_.toUser)

  def deleteUser(id: Long, session: String): IO[Int] = (for {
    idOpt <- SessionRepository.checkSession(session)
    res <- idOpt match {
      case Some(_id) if id == _id => UserRepository.deleteAcc(id)
      case _ =>  throw new Exception("???")
    }
  } yield res).transact(xa)

  def login(req: LoginReq): IO[String] =
    UserRepository.findByLogin(req.login).flatMap{
      case Some(user) if user.password == req.password =>
        UserRepository.createSession(user.id, UUID.randomUUID().toString)
      case _ => throw new Exception("Invalid login/password")
    }.transact(xa)
}
