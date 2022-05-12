package session

import doobie.free.connection.ConnectionIO

trait SessionRepository {
  def checkSession(session: String): ConnectionIO[Option[Long]]
}


