package moderator

import cats.effect.IO
import data.dto.{Lesson, User}

trait ModeratorRepository {

    def isMod(userId: Long): IO[Boolean]

    def blockUser(userId: Long): IO[Boolean]

    def closeAllSessions(userId: Long): IO[Int]

    def unblockUser(userId: Long): IO[Boolean]

    def deleteUser(userId: Long): IO[Boolean]

    def deleteLesson(lessonId: Long): IO[Boolean]

    def lessonById(lessonId: Long): IO[Lesson]

    def userById(userId: Long): IO[User]


}
