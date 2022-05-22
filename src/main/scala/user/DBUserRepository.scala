package user

import cats.effect.IO
import config.DI.ResTransactor
import data.dao.UserDao
import data.dto.{Balance, User}
import data.req._
import doobie.free.implicits._
import doobie.implicits._
import util.ApiErrors._
import util.Util.dbConnection
import util.dictionary.UserDictionary

import java.util.UUID

class DBUserRepository(xa: ResTransactor[IO]) extends UserRepository {


  override def getUserById(id: Long): IO[Option[UserDao]] = UserDictionary.findById(id).cast(xa)

  override def createUser(insert: User.Insert): IO[UserDao] = UserDictionary.create(insert).cast(xa)

  override def deleteById(id: Long): IO[Int] =
    UserDictionary.deleteAcc(id).cast(xa)

  def findByLoginNonBlocked(req: LoginReq): IO[Option[UserDao]] = UserDictionary.findByLoginNonBlocked(req.login)
    .cast(xa)


  override def login(user: User): IO[String] =
    UserDictionary.createSession(user.id, UUID.randomUUID().toString).cast(xa)


  override def logout(session: String): IO[Int] = UserDictionary.logout(session).cast(xa)


  override def cashIn(id: Long, amount: BigDecimal): IO[Balance] = UserDictionary.cashIn(id, amount).cast(xa)

  override def balance(userId: Long): IO[Balance] = UserDictionary.balance(userId).cast(xa)

  override def withdrawal(userId: Long, amount: BigDecimal): IO[Balance] =
    UserDictionary.withdrawal(userId, amount).cast(xa)
}
