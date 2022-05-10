package session

import cats.effect.IO
import doobie.util.transactor.Transactor.Aux
import util.Implicits._


class SessionService(implicit xa: Aux[IO, Unit]) {
  def checkSession(session: String): IO[Long] = SessionRepository.checkSession(session).map {
    case Some(id) => id
    case None => throw new IllegalAccessException("Invalid session")
  }
}
