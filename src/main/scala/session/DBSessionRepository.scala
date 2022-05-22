package session

import cats.effect.IO
import config.DI.ResTransactor
import doobie.implicits._
import util.Implicits._
import util.Util.dbConnection
import util.dictionary.SessionDictionary


class DBSessionRepository(xa: ResTransactor[IO]) extends SessionRepository {
  def getIdBySession(session: String): IO[Option[Long]] = SessionDictionary.getIdBySession(session).cast(xa)
}
