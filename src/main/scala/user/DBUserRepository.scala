package user

import cats.effect.IO
import data.dao.UserDao
import data.dto.{Balance, User}
import data.req._
import doobie.free.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors._
import util.dictionary.UserDictionary

import java.util.UUID

class DBUserRepository(xa: Aux[IO, Unit]) extends UserRepository {


  override def getUserById(id: Long): IO[Option[UserDao]] = UserDictionary.findById(id).transact(xa)

  override def createUser(insert: User.Insert): IO[UserDao] = UserDictionary.create(insert).transact(xa)

  override def deleteById(id: Long): IO[Int] =
    UserDictionary.deleteAcc(id).transact(xa)

  def findByLoginNonBlocked(req: LoginReq): IO[Option[UserDao]] = UserDictionary.findByLoginNonBlocked(req.login)
    .transact(xa)


  override def login(user: User): IO[String] =
    UserDictionary.createSession(user.id, UUID.randomUUID().toString).transact(xa)


  override def logout(session: String): IO[Int] = UserDictionary.logout(session).transact(xa)


  override def cashIn(id: Long, amount: BigDecimal): IO[Balance] = UserDictionary.cashIn(id, amount).transact(xa)

  override def balance(userId: Long): IO[Balance] = UserDictionary.balance(userId).transact(xa)

  override def withdrawal(userId: Long, amount: BigDecimal): IO[Balance] =
    UserDictionary.withdrawal(userId, amount).transact(xa)
}
