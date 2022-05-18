package session

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import util.ApiErrors.InvalidSessionError
import util.Implicits._
import util.dictionary.SessionDictionary


class DBSessionRepository(xa: Aux[IO, Unit]) extends SessionRepository {
  def getIdBySession(session: String): IO[Long] = SessionDictionary.getIdBySession(session).transact(xa)
    .map {
    case Some(id) => id
    case None => throw InvalidSessionError
  }
}
