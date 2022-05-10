package session

import doobie.free.connection.ConnectionIO
import doobie.implicits.toSqlInterpolator

object SessionRepository {

  def checkSession(session: String): ConnectionIO[Option[Long]] =
    sql"select user_id from session where session.session = $session".query[Long].option

}
