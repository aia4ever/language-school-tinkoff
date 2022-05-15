package moderator

import data.dao.UserDao
import data.dto.Lesson
import doobie.free.connection.ConnectionIO

trait ModeratorRepository {

    def isMod(userId: Long): ConnectionIO[Boolean]

    def blockUser(userId: Long): ConnectionIO[Boolean]

    def closeAllSessions(userId: Long): ConnectionIO[Int]

    def unblockUser(userId: Long): ConnectionIO[Boolean]

    def deleteUser(userId: Long): ConnectionIO[Boolean]

    def deleteLesson(userId: Long): ConnectionIO[Boolean]

    def lessonById(lessonId: Long): ConnectionIO[Option[Lesson]]

    def userById(userId: Long): ConnectionIO[Option[UserDao]]
}
