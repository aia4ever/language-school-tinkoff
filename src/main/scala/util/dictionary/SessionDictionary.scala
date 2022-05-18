package util.dictionary

import doobie.free.connection.ConnectionIO
import doobie.implicits._

object SessionDictionary{
  def getIdBySession(session: String): ConnectionIO[Option[Long]] =
    sql"select user_id from session where session.session = $session".query[Long].option
}