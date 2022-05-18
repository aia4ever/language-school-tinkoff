package user

import cats.effect.IO
import data.dto.{Balance, User}
import data.req._
import doobie.free.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors._
import util.dictionary.UserDictionary

import java.util.UUID

class DBUserRepository(xa: Aux[IO, Unit]) extends UserRepository {


  override def getUserById(id: Long): IO[User] = UserDictionary.findById(id).transact(xa).map {
    case Some(us) => us.toUser
    case None => throw UserNotFoundError
  }

  override def createUser(insert: User.Insert): IO[User] = UserDictionary.create(insert).transact(xa).map(_.toUser)

  override def deleteById(id: Long): IO[Int] =
    UserDictionary.deleteAcc(id).transact(xa)

  def findByLoginNonBlocked(req: LoginReq): IO[User] = UserDictionary.findByLoginNonBlocked(req.login)
    .transact(xa).map {
    case Some(userDao) => userDao.toUser
    case None => throw InvalidLoginPasswordError
  }


  override def login(user: User): IO[String] =
    UserDictionary.createSession(user.id, UUID.randomUUID().toString).transact(xa)


  override def logout(session: String): IO[Unit] = UserDictionary.logout(session).transact(xa).map {
    case 1 => ()
    case 0 => throw InvalidSessionError
  }

  override def cashIn(id: Long, amount: BigDecimal): IO[Balance] = UserDictionary.cashIn(id, amount).transact(xa)

  override def balance(userId: Long): IO[Balance] = UserDictionary.balance(userId).transact(xa)

  override def withdrawal(userId: Long, amount: BigDecimal): IO[Balance] =
    UserDictionary.withdrawal(userId, amount).transact(xa)
}
