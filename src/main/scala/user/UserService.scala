package user

import cats.effect.IO
import data.req._
import doobie.free.implicits._
import doobie.util.transactor.Transactor.Aux
import session.SessionRepository
import util.Implicits.cast

import java.util.UUID

class UserService(implicit val xa: Aux[IO, Unit]) {

  def userNotFound = throw new Exception("User not found")

  def logout(logoutReq: LogoutReq): IO[Unit] = UserRepository.logout(logoutReq.session).map {
    case 1 => ()
    case 0 => throw new IllegalAccessException("Invalid session")
  }

  def byLogin(login: String): IO[User] = UserRepository.findByLogin(login).map {
    case Some(us) => us.toUser
    case None => userNotFound
  }
  def byId(id: Long): IO[User] = UserRepository.findById(id).map {
    case Some(us) => us.toUser
    case None => userNotFound
  }


  def create(insert: User.Insert): IO[User] = UserRepository.create(insert).map(_.toUser)

  def deleteUser(id: Long, session: String): IO[Int] = for {
    idOpt <- cast(SessionRepository.checkSession(session))
    res <- idOpt match {
      case Some(_id) if id == _id => cast(UserRepository.deleteAcc(id))
      case _ =>  IO.raiseError(throw new IllegalArgumentException("???"))
    }
  } yield res

  def login(req: LoginReq): IO[String] =
    UserRepository.findByLogin(req.login).flatMap{
      case Some(user) if user.password == req.password =>
        UserRepository.createSession(user.id, UUID.randomUUID().toString)
      case _ => throw new IllegalArgumentException("Invalid login/password")
    }


//    UserRepository.findByLogin(req.login).map {
//        case Some(user) => user match {
//          case u if u.password == req.password =>
//            UserRepository.createSession(user.id, UUID.randomUUID().toString)
//          case _ => throw new IllegalArgumentException("Invalid password")
//        }
//        case None => throw new IllegalArgumentException("Invalid login")
//
//      }

}
