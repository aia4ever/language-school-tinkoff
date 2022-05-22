import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.dao.{TeacherDao, UserDao}
import data.dto.{Balance, Lesson, User}
import data.req.{BioReq, LessonUpdateReq, WithdrawalReq}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import session.SessionRepository
import teacher.{TeacherRepository, TeacherService}
import user.UserRepository
import util.{Male, StudentType, TeacherType}

import java.time.Instant
import java.time.temporal.ChronoUnit

class TeacherServiceTest extends AnyFlatSpec with MockitoSugar with BeforeAndAfterEach {

  var teacherRepoMock: TeacherRepository = mock[TeacherRepository]
  var userRepoMock: UserRepository = mock[UserRepository]
  var sessionRepoMock: SessionRepository = mock[SessionRepository]

  var teacherService = new TeacherService(teacherRepoMock, userRepoMock, sessionRepoMock)

  val validSession1 = "valid session1"
  val validSession2 = "valid session2"
  val validSession3 = "valid session3"
  val invalidSession = "invalid session"

  val invalidTarget = 1000L

  val teacher1: User = User(1, "test", "test", "test", "test", "test", "test", Male, TeacherType)
  val teacherDao1: TeacherDao = TeacherDao(1, "test", "test", Male.toString, Some("bio"), Some(5), Some(5))

  val teacher2: User = User(2, "test2", "test2", "test2", "test2", "test2", "test2", Male, TeacherType)
  val teacherDao2: TeacherDao = TeacherDao(1, "test2", "test2", Male.toString, Some("bio"), Some(5), Some(5))

  val student1: User = User(14, "test3", "test3", "test3", "test3", "test3", "test3", Male, StudentType)
  val studentDao1: UserDao = UserDao(14, "test3", "test3", "test3", "test3", "test3", "test3", Male.toString, StudentType.toString)

  val lesson1: Lesson = Lesson(1, teacher1.id, Instant.now().minus(2, ChronoUnit.DAYS), 10, "zoom", Some(student1.id), Some("test"), Some("test"), Some(5.0), purchased = false)
  val lesson2: Lesson = Lesson(2, teacher1.id, Instant.now().plus(2, ChronoUnit.DAYS), 10, "zoom", Option.empty[Long], Option.empty[String], Option.empty[String], Option.empty[Double], purchased = false)
  val lesson3: Lesson = Lesson(3, teacher1.id, Instant.now().plus(2, ChronoUnit.DAYS), 10, "zoom", Some(student1.id), Option.empty[String], Some("test"), Option.empty[Double], purchased = false)
  val lesson4: Lesson = Lesson(4, teacher1.id, Instant.now().plus(2, ChronoUnit.DAYS), 10, "zoom", Option.empty[Long], Option.empty[String], Option.empty[String], Option.empty[Double], purchased = false)
  val lesson5: Lesson = Lesson(5, teacher1.id, Instant.now().minus(2, ChronoUnit.DAYS), 10, "zoom", Some(student1.id), Some("test"), Some("test"), Some(5.0), purchased = true)
  val lesson6: Lesson = Lesson(6, teacher1.id, Instant.now().minus(2, ChronoUnit.DAYS), 10, "zoom", Option.empty[Long], Some("test"), Some("test"), Some(5.0), purchased = false)

  val balance1: Balance = Balance(lesson1.price, lesson1.price)
  val balance2: Balance = Balance(lesson1.price - 1, lesson1.price)

  val lessonUpdate: LessonUpdateReq = LessonUpdateReq(lesson1.id, "", "", 0)
  val invalidLessonUpdate: LessonUpdateReq = LessonUpdateReq(invalidTarget, "", "", 0)

  val bio = "bio"

  override def beforeEach(): Unit = {
    userRepoMock = mock[UserRepository]
    sessionRepoMock = mock[SessionRepository]
    teacherRepoMock = mock[TeacherRepository]

    teacherService = new TeacherService(teacherRepoMock, userRepoMock, sessionRepoMock)

    when(sessionRepoMock.getIdBySession(validSession1)).thenReturn(IO.pure(Some(teacher1.id)))
    when(sessionRepoMock.getIdBySession(validSession2)).thenReturn(IO.pure(Some(teacher2.id)))
    when(sessionRepoMock.getIdBySession(validSession3)).thenReturn(IO.pure(Some(student1.id)))
    when(sessionRepoMock.getIdBySession(invalidSession)).thenReturn(IO.pure(Option.empty[Long]))

    when(teacherRepoMock.getTeacher(teacher1.id)).thenReturn(IO.pure(Some(teacherDao1)))
    when(teacherRepoMock.getTeacher(teacher2.id)).thenReturn(IO.pure(Some(teacherDao2)))
    when(teacherRepoMock.getTeacher(student1.id)).thenReturn(IO.pure(Option.empty[TeacherDao]))
    when(teacherRepoMock.getTeacher(invalidTarget)).thenReturn(IO.pure(Option.empty[TeacherDao]))

    when(teacherRepoMock.getLessonsByDate(teacher1.id, lesson4.date)).thenReturn(IO.pure(List(lesson2)))
    when(teacherRepoMock.getLessonsByDate(teacher2.id, lesson4.date)).thenReturn(IO.pure(List.empty[Lesson]))

    when(teacherRepoMock.newLesson(lesson4.toInsert)).thenReturn(IO.pure(lesson4))

    when(teacherRepoMock.bioUpdate(teacher1.id, bio)).thenReturn(IO.pure(1))

    when(teacherRepoMock.upcomingLessons(teacher1.id)).thenReturn(IO.pure(List(lesson2)))
    when(teacherRepoMock.upcomingLessons(teacher2.id)).thenReturn(IO.pure(List.empty[Lesson]))

    when(teacherRepoMock.previousLessons(teacher1.id)).thenReturn(IO.pure(List(lesson1)))

    when(teacherRepoMock.getLesson(lesson1.id, teacher1.id)).thenReturn(IO.pure(Some(lesson1)))
    when(teacherRepoMock.getLesson(lesson1.id, teacher2.id)).thenReturn(IO.pure(Option.empty[Lesson]))

    when(teacherRepoMock.updateLesson(lessonUpdate)).thenReturn(IO.pure(lesson1))

    when(teacherRepoMock.deleteLesson(lesson1.id, teacher1.id)).thenReturn(IO.pure(1))
    when(teacherRepoMock.deleteLesson(lesson1.id, teacher2.id)).thenReturn(IO.pure(0))

    when(userRepoMock.balance(teacher1.id)).thenReturn(IO.pure(balance1))

    when(userRepoMock.withdrawal(teacher1.id, balance1.amount)).thenReturn(IO.pure(balance1))

    when(teacherRepoMock.updateLessonStatus(any[Long], any[Long])).thenReturn(IO.pure(1))
    when(teacherRepoMock.payment(any[Lesson])).thenReturn(IO.pure(1))
  }


  behavior of "create lesson"

  it should "create lesson" in {
    teacherService.createLesson(validSession2, lesson4.toInsert).unsafeRunSync() shouldBe lesson4
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).getTeacher(teacher2.id)
    verify(teacherRepoMock, times(1)).getLessonsByDate(teacher2.id, lesson4.date)
    verify(teacherRepoMock, times(1)).newLesson(lesson4.toInsert)
  }

  it should "throw Exception if user has another activity" in {
    assertThrows[Exception](teacherService.createLesson(validSession1, lesson4.toInsert).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).getLessonsByDate(teacher1.id, lesson4.date)
    verify(teacherRepoMock, times(0)).newLesson(lesson4.toInsert)
  }

  it should "throw Exception if user is not teacher" in {
    assertThrows[Exception](teacherService.createLesson(validSession3, lesson4.toInsert).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession3)
    verify(teacherRepoMock, times(1)).getTeacher(student1.id)
    verify(teacherRepoMock, times(0)).getLessonsByDate(student1.id, lesson4.date)
    verify(teacherRepoMock, times(0)).newLesson(lesson4.toInsert)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.createLesson(invalidSession, lesson4.toInsert).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).getTeacher(student1.id)
    verify(teacherRepoMock, times(0)).getLessonsByDate(student1.id, lesson4.date)
    verify(teacherRepoMock, times(0)).newLesson(lesson4.toInsert)
  }

  behavior of "bio update"
  it should "update bio" in {
    teacherService.bioUpdate(validSession1, BioReq(bio)).unsafeRunSync() shouldBe teacherDao1.toTeacher
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(2)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).bioUpdate(teacher1.id, bio)
  }

  it should "throw Exception if user is not teacher" in {
    assertThrows[Exception](teacherService.bioUpdate(validSession3, BioReq(bio)).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession3)
    verify(teacherRepoMock, times(1)).getTeacher(student1.id)
    verify(teacherRepoMock, times(0)).bioUpdate(student1.id, bio)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.bioUpdate(invalidSession, BioReq(bio)).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(0)).bioUpdate(teacher1.id, bio)
  }

  behavior of "upcoming"

  it should "return list of upcoming lessons" in {
    teacherService.upcoming(validSession2).unsafeRunSync() shouldBe List()
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).upcomingLessons(teacher2.id)
  }
  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.upcoming(invalidSession).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).upcomingLessons(teacher2.id)
  }

  behavior of "next"

  it should "return next lesson" in {
    teacherService.next(validSession1).unsafeRunSync() shouldBe lesson2
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).upcomingLessons(teacher1.id)
  }

  it should "throw Exception if user doesn't have next lesson" in {
    assertThrows[Exception](teacherService.next(validSession2).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).upcomingLessons(teacher2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.next(invalidSession).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).upcomingLessons(teacher2.id)
  }

  behavior of "previous"

  it should "return list of previous lesson" in {
    teacherService.previous(validSession1).unsafeRunSync() shouldBe List(lesson1)
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).previousLessons(teacher1.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.previous(invalidSession).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).previousLessons(teacher1.id)
  }

  behavior of "get your lesson"

  it should "return next lesson" in {
    teacherService.getYourLesson(validSession1, lesson1.id).unsafeRunSync() shouldBe lesson1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getLesson(lesson1.id, teacher1.id)
  }

  it should "throw Exception if user is not teacher in lesson" in {
    assertThrows[Exception](teacherService.getYourLesson(validSession2, lesson1.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).getLesson(lesson1.id, teacher2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.getYourLesson(invalidSession, lesson1.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).getLesson(teacher2.id, lesson1.id)
  }

  behavior of "update lesson"

  it should "update lesson" in {
    teacherService.updateLesson(validSession1, lessonUpdate).unsafeRunSync() shouldBe lesson1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getLesson(lessonUpdate.id, teacher1.id)
    verify(teacherRepoMock, times(1)).updateLesson(lessonUpdate)
  }

  it should "throw Exception if teacher is invalid" in {
    assertThrows[Exception](teacherService.updateLesson(validSession2, lessonUpdate).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).getLesson(lessonUpdate.id, teacher2.id)
    verify(teacherRepoMock, times(0)).updateLesson(lessonUpdate)
  }
  it should "throw Exception if lesson id is invalid" in {
    assertThrows[Exception](teacherService.updateLesson(validSession1, invalidLessonUpdate).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getLesson(invalidTarget, teacher1.id)
    verify(teacherRepoMock, times(0)).updateLesson(lessonUpdate)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.updateLesson(invalidSession, lessonUpdate).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).getLesson(lessonUpdate.id, teacher1.id)
    verify(teacherRepoMock, times(0)).updateLesson(lessonUpdate)
  }

  behavior of "delete"

  it should "delete lesson" in {
    teacherService.delete(validSession1, lesson1.id).unsafeRunSync() shouldBe 1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).deleteLesson(lesson1.id, teacher1.id)
  }

  it should "user can't delete another teacher lesson" in {
    teacherService.delete(validSession2, lesson1.id).unsafeRunSync() shouldBe 0
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).deleteLesson(lesson1.id, teacher2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.delete(invalidSession, lesson1.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).deleteLesson(lesson1.id, teacher1.id)
  }

  behavior of "withdrawal"

  it should "withdrawal" in {
    teacherService.withdrawal(validSession1, WithdrawalReq(balance1.amount)).unsafeRunSync() shouldBe balance1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(userRepoMock, times(1)).balance(teacher1.id)
    verify(userRepoMock, times(1)).withdrawal(teacher1.id, balance1.amount)
  }

  it should "throw Exception if there are insufficient funds" in {
    assertThrows[Exception](teacherService.withdrawal(validSession1, WithdrawalReq(balance1.amount + 1)).unsafeRunSync() shouldBe balance1)
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(userRepoMock, times(1)).balance(teacher1.id)
    verify(userRepoMock, times(0)).withdrawal(teacher1.id, balance1.amount + 1)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.withdrawal(invalidSession, WithdrawalReq(balance1.amount)).unsafeRunSync() shouldBe balance1)
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(userRepoMock, times(0)).balance(teacher1.id)
    verify(userRepoMock, times(0)).withdrawal(teacher1.id, balance1.amount)
  }

  behavior of "payment"

  it should "make payment" in {
    teacherService.payment(validSession1, lesson1.id).unsafeRunSync() shouldBe 1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).getLesson(lesson1.id,teacher1.id)
    verify(teacherRepoMock, times(1)).updateLessonStatus(lesson1.id, teacher1.id)
    verify(teacherRepoMock, times(1)).payment(lesson1)
  }

  it should "throw Exception if lesson is already paid" in {
   assertThrows[Exception](teacherService.payment(validSession1, lesson5.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).getLesson(lesson5.id,teacher1.id)
    verify(teacherRepoMock, times(0)).updateLessonStatus(lesson5.id, teacher1.id)
    verify(teacherRepoMock, times(0)).payment(lesson5)
  }

  it should "throw Exception if student is empty" in {
    assertThrows[Exception](teacherService.payment(validSession1, lesson6.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).getLesson(lesson6.id,teacher1.id)
    verify(teacherRepoMock, times(0)).updateLessonStatus(lesson6.id, teacher1.id)
    verify(teacherRepoMock, times(0)).payment(lesson6)
  }

  it should "throw Exception if user cant access lesson" in {
    assertThrows[Exception](teacherService.payment(validSession2, lesson6.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(teacherRepoMock, times(1)).getTeacher(teacher2.id)
    verify(teacherRepoMock, times(1)).getLesson(lesson6.id,teacher2.id)
    verify(teacherRepoMock, times(0)).updateLessonStatus(lesson6.id, teacher2.id)
    verify(teacherRepoMock, times(0)).payment(lesson6)
  }

  it should "throw Exception if user is not teacher" in {
    assertThrows[Exception](teacherService.payment(validSession3, lesson6.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession3)
    verify(teacherRepoMock, times(1)).getTeacher(student1.id)
    verify(teacherRepoMock, times(0)).getLesson(lesson6.id,student1.id)
    verify(teacherRepoMock, times(0)).updateLessonStatus(lesson6.id, student1.id)
    verify(teacherRepoMock, times(0)).payment(lesson6)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](teacherService.payment(invalidSession, lesson6.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(teacherRepoMock, times(0)).getTeacher(lesson6.teacherId)
    verify(teacherRepoMock, times(0)).getLesson(lesson6.id,lesson6.teacherId)
    verify(teacherRepoMock, times(0)).updateLessonStatus(lesson6.id, lesson6.teacherId)
    verify(teacherRepoMock, times(0)).payment(lesson6)
  }
}
