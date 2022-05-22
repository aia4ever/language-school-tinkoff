import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.dao.UserDao
import data.dto.{Lesson, User}
import moderator.{ModeratorRepository, ModeratorService}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import session.SessionRepository
import util.{Female, Male, ModeratorType, StudentType}

import java.time.Instant

class ModeratorServiceTest extends AnyFlatSpec with MockitoSugar with BeforeAndAfterEach{

  val moderator1: User =  User(12, "test1","test1","test1","test1","test1","test1", Male, ModeratorType)
  val moderator1Dao: UserDao =  UserDao(12, "test1","test1","test1","test1","test1","test1", Male.toString, ModeratorType.toString)

  val moderator2: User =  User(13, "test2","test2","test2","test2","test2","test2", Female, ModeratorType)
  val moderator2Dao: UserDao =  UserDao(13, "test2","test2","test2","test2","test2","test2", Female.toString, ModeratorType.toString)

  val student1: User =  User(14, "test3","test3","test3","test3","test3","test3", Male, StudentType)
  val studentDao1: UserDao =  UserDao(14, "test3","test3","test3","test3","test3","test3", Male.toString, StudentType.toString)

  // blocked User
  val student2: User =  User(15, "test5","test5","test5","test5","test5","test5", Male, StudentType)
  val studentDao2: UserDao =  UserDao(15, "test5","test5","test5","test5","test5","test5", Male.toString, StudentType.toString)


  val lesson: Lesson = Lesson(1, 2, Instant.now(), 10, "zoom", Some(1), Some("test"),Some("test"), Some(5.0), purchased = false)

  val validSession1 = "valid session1"
  val validSession2 = "valid session2"
  val invalidSession = "invalid session"

  val invalidTarget = 1000L

  var moderatorRepoMock: ModeratorRepository = mock[ModeratorRepository]

  var sessionRepoMock: SessionRepository = mock[SessionRepository]

  var moderatorService = new ModeratorService(moderatorRepoMock, sessionRepoMock)

  override def beforeEach(): Unit = {
    moderatorRepoMock = mock[ModeratorRepository]

    sessionRepoMock = mock[SessionRepository]

    moderatorService = new ModeratorService(moderatorRepoMock, sessionRepoMock)

    when(sessionRepoMock.getIdBySession(validSession1)).thenReturn(IO.pure(Some(moderator1.id)))
    when(sessionRepoMock.getIdBySession(validSession2)).thenReturn(IO.pure(Some(student1.id)))
    when(sessionRepoMock.getIdBySession(invalidSession)).thenReturn(IO.pure(Option.empty[Long]))

    when(moderatorRepoMock.userById(moderator1.id)).thenReturn(IO.pure(Some(moderator1Dao)))
    when(moderatorRepoMock.userById(moderator2.id)).thenReturn(IO.pure(Some(moderator2Dao)))
    when(moderatorRepoMock.userById(student1.id)).thenReturn(IO.pure(Some(studentDao1)))
    when(moderatorRepoMock.userById(student2.id)).thenReturn(IO.pure(Some(studentDao2)))
    when(moderatorRepoMock.userById(invalidTarget)).thenReturn(IO.pure(Option.empty[UserDao]))

    when(moderatorRepoMock.blockUser(student1.id)).thenReturn(IO.pure(1))
    when(moderatorRepoMock.blockUser(student2.id)).thenReturn(IO.pure(0))
    when(moderatorRepoMock.blockUser(invalidTarget)).thenReturn(IO.pure(0))

    when(moderatorRepoMock.unblockUser(student1.id)).thenReturn(IO.pure(0))
    when(moderatorRepoMock.unblockUser(student2.id)).thenReturn(IO.pure(1))
    when(moderatorRepoMock.unblockUser(invalidTarget)).thenReturn(IO.pure(0))

    when(moderatorRepoMock.deleteUser(any)).thenReturn(IO.pure(1))
    when(moderatorRepoMock.deleteUser(invalidTarget)).thenReturn(IO.pure(0))

    when(moderatorRepoMock.deleteLesson(any)).thenReturn(IO.pure(1))
    when(moderatorRepoMock.deleteLesson(invalidTarget)).thenReturn(IO.pure(0))

    when(moderatorRepoMock.userById(student1.id)).thenReturn(IO.pure(Some(studentDao1)))
    when(moderatorRepoMock.userById(invalidTarget)).thenReturn(IO.pure(Option.empty[UserDao]))

    when(moderatorRepoMock.lessonById(lesson.id)).thenReturn(IO.pure(Some(lesson)))
    when(moderatorRepoMock.lessonById(invalidTarget)).thenReturn(IO.pure(Option.empty[Lesson]))
  }

  behavior of "blockUser"

  it should "block valid target" in {

    val res = moderatorService.blockUser(validSession1, student1.id).unsafeRunSync()
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock,times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(1)).blockUser(student1.id)
    res  shouldBe true
  }

  it should "throw Exception if target is moderator" in {
    assertThrows[Exception](moderatorService.blockUser(validSession1, moderator2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock,times(1)).userById(moderator2.id)
    verify(moderatorRepoMock, times(0)).blockUser(moderator2.id)
  }

  it should "throw Exception if user is not Mod" in {
    assertThrows[Exception](moderatorService.blockUser(validSession2, student2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock,times(1)).userById(student2.id)
    verify(moderatorRepoMock, times(0)).blockUser(student2.id)
  }

  it should "return false if target is already blocked" in {
    val res = moderatorService.blockUser(validSession1, student2.id).unsafeRunSync()
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock,times(1)).userById(student2.id)
    verify(moderatorRepoMock, times(1)).blockUser(student2.id)
    res shouldBe false
  }

  it should "return false if target doesn't exist" in {
    val res = moderatorService.blockUser(validSession1, invalidTarget).unsafeRunSync()
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock,times(1)).userById(invalidTarget)
    verify(moderatorRepoMock, times(1)).blockUser(invalidTarget)
    res shouldBe false
  }

  behavior of "unblock"

  it should "return true if target is blocked " in {
    moderatorService.unblock(validSession1, student2.id).unsafeRunSync() shouldBe true
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).unblockUser(student2.id)
  }

  it should "return false if target is not blocked" in {
    moderatorService.unblock(validSession1, student1.id).unsafeRunSync() shouldBe false
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).unblockUser(student1.id)
  }

  it should "return false if target doesn't exist" in {
    moderatorService.unblock(validSession1, invalidTarget).unsafeRunSync() shouldBe false
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).unblockUser(invalidTarget)
  }

  it should "throw Exception if user is not Mod" in {
    assertThrows[Exception](moderatorService.unblock(validSession2, student2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(0)).unblockUser(student2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](moderatorService.unblock(invalidSession, student2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(moderatorRepoMock, times(0)).userById(invalidTarget)
    verify(moderatorRepoMock, times(0)).unblockUser(student2.id)
  }

  behavior of "delete user"

  it should "return true if target is valid" in {
    moderatorService.deleteUser(validSession1, student1.id).unsafeRunSync() shouldBe true
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(1)).deleteUser(student1.id)
  }

  it should "return false if target is invalid" in {
    moderatorService.deleteUser(validSession1, invalidTarget).unsafeRunSync() shouldBe false
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).userById(invalidTarget)
    verify(moderatorRepoMock, times(1)).deleteUser(invalidTarget)
  }

  it should "throw Exception if user is not Mod" in {
    assertThrows[Exception](moderatorService.deleteUser(validSession2, student2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(1)).userById(student2.id)
    verify(moderatorRepoMock, times(0)).deleteUser(student2.id)

  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](moderatorService.deleteUser(invalidSession, student1.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(moderatorRepoMock, times(0)).userById(moderator1.id)
    verify(moderatorRepoMock, times(0)).userById(student1.id)
    verify(moderatorRepoMock, times(0)).deleteUser(student1.id)
  }

  it should "throw Exception if target is moderator" in {
    assertThrows[Exception](moderatorService.deleteUser(validSession1, moderator2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).userById(moderator2.id)
    verify(moderatorRepoMock, times(0)).deleteUser(moderator2.id)
  }

  behavior of "delete lesson"

  it should "return true if target is valid" in {
    moderatorService.deleteLesson(validSession1, lesson.id).unsafeRunSync() shouldBe true
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).deleteLesson(lesson.id)
  }

  it should "return false if target is invalid" in {
    moderatorService.deleteLesson(validSession1, invalidTarget).unsafeRunSync() shouldBe false
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).deleteLesson(invalidTarget)
  }

  it should "throw Exception if user is not Mod" in {
    assertThrows[Exception](moderatorService.deleteLesson(validSession2, lesson.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(0)).deleteLesson(lesson.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](moderatorService.deleteUser(invalidSession, lesson.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(moderatorRepoMock, times(0)).userById(moderator1.id)
    verify(moderatorRepoMock, times(0)).deleteLesson(lesson.id)
  }

  behavior of "get user"

  it should "throw Exception if user is not Mod" in {
    assertThrows[Exception](moderatorService.getUser(validSession2, student2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(0)).userById(student2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](moderatorService.getUser(invalidSession, student1.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(moderatorRepoMock, times(0)).userById(moderator1.id)
    verify(moderatorRepoMock, times(0)).userById(student1.id)
  }

  it should "return user" in {
    moderatorService.getUser(validSession1, student1.id).unsafeRunSync() shouldBe student1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
  }

  it should "throw Exception if target is invalid" in {
    assertThrows[Exception](moderatorService.getUser(validSession1, invalidTarget).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).userById(invalidTarget)
  }

  behavior of "get lesson"

  it should "throw Exception if user is not Mod" in {
    assertThrows[Exception](moderatorService.getLesson(validSession2, lesson.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(moderatorRepoMock, times(1)).userById(student1.id)
    verify(moderatorRepoMock, times(0)).lessonById(lesson.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](moderatorService.getLesson(invalidSession, lesson.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(moderatorRepoMock, times(0)).userById(moderator1.id)
    verify(moderatorRepoMock, times(0)).lessonById(lesson.id)
  }

  it should "return user" in {
    moderatorService.getLesson(validSession1, lesson.id).unsafeRunSync() shouldBe lesson
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).lessonById(lesson.id)
  }

  it should "throw Exception if target is invalid" in {
    assertThrows[Exception](moderatorService.getLesson(validSession1, invalidTarget).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(moderatorRepoMock, times(1)).userById(moderator1.id)
    verify(moderatorRepoMock, times(1)).lessonById(invalidTarget)
  }
}
