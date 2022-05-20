package moderator

import cats.effect.IO
import data.dao.UserDao
import data.dto.Lesson

trait ModeratorRepository {

    def blockUser(userId: Long): IO[Int]

    def closeAllSessions(userId: Long): IO[Int]

    def unblockUser(userId: Long): IO[Int]

    def deleteUser(userId: Long): IO[Int]

    def deleteLesson(lessonId: Long): IO[Int]

    def lessonById(lessonId: Long): IO[Option[Lesson]]

    def userById(userId: Long): IO[Option[UserDao]]


}
