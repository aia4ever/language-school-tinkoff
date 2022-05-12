package session

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.Implicits._


class SessionService(xa: Aux[IO, Unit], sRep: SessionRepository) {
  def checkSession(s: String): IO[Long] = sRep.checkSession(s).transact(xa)
    .map {
    case Some(id) => id
    case None => throw new IllegalAccessException("Invalid session")
  }
}
