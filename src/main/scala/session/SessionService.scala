package session

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.Implicits._


class SessionService(xa: Aux[IO, Unit]) {


  def checkSession(session: String): IO[Long] = SessionRepository.checkSession(session).transact(xa)
    .map {
    case Some(id) => id
    case None => throw new IllegalAccessException("Invalid session")
  }
}
