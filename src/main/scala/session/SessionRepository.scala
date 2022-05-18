package session

import cats.effect.IO

trait SessionRepository {
  def getIdBySession(session: String): IO[Long]
}


