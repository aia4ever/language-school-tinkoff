package user

import cats.effect.IO
import data.dto.User
import data.req._
import doobie.free.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import session.SessionRepository

import java.util.UUID

class UserService(xa: Aux[IO, Unit], uRep: UserRepository, sRep: SessionRepository) {

  def userNotFound = throw new Exception("User not found")

  def logout(s: String): IO[Unit] = uRep.logout(s).transact(xa).map {
    case 1 => ()
    case 0 => new Exception("Invalid session")
  }

  def byId(id: Long): IO[User] = uRep.findById(id).transact(xa).map {
    case Some(us) => us.toUser
    case None => userNotFound
  }


  def create(insert: User.Insert): IO[User] = uRep.create(insert).transact(xa).map(_.toUser)

  def deleteUser(id: Long, s: String): IO[Int] = (for {
    idOpt <- sRep.checkSession(s)
    res <- idOpt match {
      case Some(_id) if id == _id => uRep.deleteAcc(id)
      case _ =>  throw new Exception("???")
    }
  } yield res).transact(xa)

  def login(req: LoginReq): IO[String] =
    uRep.findByLoginNonBlocked(req.login).flatMap{
      case Some(user) if user.password == req.password =>
        uRep.createSession(user.id, UUID.randomUUID().toString)
      case _ => throw new Exception("Invalid login/password")
    }.transact(xa)
}
