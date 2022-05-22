import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.dao.{TeacherDao, UserDao}
import data.dto.{Balance, Lesson, User}
import data.req.{CashInReq, GradeReq, HomeworkReq}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import session.SessionRepository
import student.{StudentRepository, StudentService}
import teacher.TeacherRepository
import user.UserRepository
import util.{Male, StudentType, TeacherType}

import java.time.Instant
import java.time.temporal.ChronoUnit

class StudentServiceTest extends AnyFlatSpec with MockitoSugar with BeforeAndAfterEach{

  var userRepoMock: UserRepository = mock[UserRepository]
  var studentRepoMock: StudentRepository = mock[StudentRepository]
  var sessionRepoMock: SessionRepository = mock[SessionRepository]
  var teacherRepoMock: TeacherRepository = mock[TeacherRepository]

  var studentService = new StudentService(userRepoMock, studentRepoMock, sessionRepoMock, teacherRepoMock)

  val student1: User = User(14, "test3", "test3", "test3", "test3", "test3", "test3", Male, StudentType)
  val studentDao1: UserDao = UserDao(14, "test3", "test3", "test3", "test3", "test3", "test3", Male.toString, StudentType.toString)

  val student2: User = User(15, "test5", "test5", "test5", "test5", "test5", "test5", Male, StudentType)
  val studentDao2: UserDao = UserDao(15, "test5", "test5", "test5", "test5", "test5", "test5", Male.toString, StudentType.toString)

  val teacher1: User = User(1, "test", "test", "test", "test", "test", "test", Male, TeacherType)
  val teacherDao1: TeacherDao = TeacherDao(1, "test",  "test", Male.toString, Some("bio"), Some(5), Some(5))

  val teacher2: User = User(2, "test2", "test2", "test2", "test2", "test2", "test2", Male, TeacherType)
  val teacherDao2: UserDao = UserDao(2, "test2", "test2", "test2", "test2", "test2", "test2", Male.toString, TeacherType.toString)


  val lesson1: Lesson = Lesson(1, teacher1.id, Instant.now().minus(2, ChronoUnit.DAYS), 10, "zoom", Some(student1.id), Some("test"), Some("test"), Some(5.0), purchased = true)
  val lesson2: Lesson = Lesson(2, teacher1.id, Instant.now().plus(2, ChronoUnit.DAYS), 10, "zoom", Option.empty[Long], Option.empty[String], Option.empty[String], Option.empty[Double], purchased = false)
  val lesson3: Lesson = Lesson(3, teacher1.id, Instant.now().plus(2, ChronoUnit.DAYS), 10, "zoom", Some(student1.id), Option.empty[String], Some("test"), Option.empty[Double], purchased = false)

  val balance1: Balance = Balance(lesson1.price, lesson1.price)
  val balance2: Balance = Balance(lesson1.price - 1, lesson1.price)

  val validSession1 = "valid session1"
  val validSession2 = "valid session2"
  val validSession3 = "valid session3"
  val invalidSession = "invalid session"

  val invalidTarget = 1000L

  override def beforeEach(): Unit = {
    userRepoMock = mock[UserRepository]
    studentRepoMock = mock[StudentRepository]
    sessionRepoMock = mock[SessionRepository]
    teacherRepoMock = mock[TeacherRepository]

    studentService = new StudentService(userRepoMock, studentRepoMock, sessionRepoMock, teacherRepoMock)

    when(sessionRepoMock.getIdBySession(validSession1)).thenReturn(IO.pure(Some(student1.id)))
    when(sessionRepoMock.getIdBySession(validSession2)).thenReturn(IO.pure(Some(student2.id)))
    when(sessionRepoMock.getIdBySession(validSession3)).thenReturn(IO.pure(Some(teacher1.id)))
    when(sessionRepoMock.getIdBySession(invalidSession)).thenReturn(IO.pure(Option.empty[Long]))

    when(studentRepoMock.getLesson(lesson1.id)).thenReturn(IO.pure(Option.empty[Lesson]))
    when(studentRepoMock.getLesson(lesson2.id)).thenReturn(IO.pure(Some(lesson2)))
    when(studentRepoMock.getLesson(lesson3.id)).thenReturn(IO.pure(Option.empty[Lesson]))
    when(studentRepoMock.getLesson(invalidTarget)).thenReturn(IO.pure(Option.empty[Lesson]))

    when(studentRepoMock.studentUserType(student1.id)).thenReturn(IO.pure(student1.userType.toString))
    when(studentRepoMock.studentUserType(student2.id)).thenReturn(IO.pure(student2.userType.toString))
    when(studentRepoMock.studentUserType(teacher1.id)).thenReturn(IO.pure(teacher1.userType.toString))

    when(teacherRepoMock.getTeacher(teacher1.id)).thenReturn(IO.pure(Some(teacherDao1)))
    when(teacherRepoMock.getTeacher(student1.id)).thenReturn(IO.pure(Option.empty[TeacherDao]))
    when(teacherRepoMock.getTeacher(invalidTarget)).thenReturn(IO.pure(Option.empty[TeacherDao]))

    when(studentRepoMock.yourLesson(lesson3.id, student1.id)).thenReturn(IO.pure(Some(lesson3)))
    when(studentRepoMock.yourLesson(lesson2.id, student1.id)).thenReturn(IO.pure(Option.empty[Lesson]))
    when(studentRepoMock.yourLesson(lesson3.id, student2.id)).thenReturn(IO.pure(Option.empty[Lesson]))

    when(studentRepoMock.getLessonsByDate(student1.id, lesson2.date)).thenReturn(IO.pure(List(lesson3)))
    when(studentRepoMock.getLessonsByDate(student2.id, lesson2.date)).thenReturn(IO.pure(List.empty[Lesson]))

    when(studentRepoMock.reserve(any, any)).thenReturn(IO.pure(1))
    when(studentRepoMock.signUp(any, any)).thenReturn(IO.pure(lesson2))

    when(studentRepoMock.studentLesson(lesson3.id, student1.id)).thenReturn(IO.pure(Some(lesson3)))
    when(studentRepoMock.studentLesson(lesson3.id, student2.id)).thenReturn(IO.pure(Option.empty[Lesson]))

    when(studentRepoMock.unreserve(any, any)).thenReturn(IO.pure(1))
    when(studentRepoMock.signOut(any, any)).thenReturn(IO.pure(1))

    when(studentRepoMock.upcoming(student1.id)).thenReturn(IO.pure(List(lesson3)))
    when(studentRepoMock.upcoming(student2.id)).thenReturn(IO.pure(List.empty[Lesson]))

    when(teacherRepoMock.teacherGrade(any)).thenReturn(IO.pure(Option.empty[(Double, Int)]))
    when(studentRepoMock.evaluateTeacherUpdate(any, any, any)).thenReturn(IO.pure(1))

    when(userRepoMock.cashIn(any, any)).thenReturn(IO.pure(balance1))

    when(studentRepoMock.homework(any, any, any)).thenReturn(IO.pure(lesson1))
  }

  behavior of "get lesson"

  it should "return empty lesson" in {
    studentService.getLesson(lesson2.id).unsafeRunSync() shouldBe lesson2
    verify(studentRepoMock, times(1)).getLesson(lesson2.id)
  }

  it should "throw Exception if lesson is not empty" in {
    assertThrows[Exception](studentService.getLesson(lesson3.id).unsafeRunSync())
    verify(studentRepoMock, times(1)).getLesson(lesson3.id)
  }

  behavior of "get your lesson"

  it should "return your lesson" in {
    studentService.getYourLesson(validSession1, lesson3.id).unsafeRunSync() shouldBe lesson3
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentUserType(student1.id)
    verify(studentRepoMock, times(1)).yourLesson(lesson3.id, student1.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.getYourLesson(invalidSession, lesson3.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).studentUserType(student1.id)
    verify(studentRepoMock, times(0)).yourLesson(lesson3.id, student1.id)
  }

  it should "throw Exception if it is another student lesson" in {
    assertThrows[Exception](studentService.getYourLesson(validSession2, lesson3.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).studentUserType(student2.id)
    verify(studentRepoMock, times(1)).yourLesson(lesson3.id, student2.id)
  }

  it should "throw Exception if user is not a Student" in {
    assertThrows[Exception](studentService.getYourLesson(validSession3, lesson3.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession3)
    verify(studentRepoMock, times(1)).studentUserType(teacher1.id)
    verify(studentRepoMock, times(0)).yourLesson(lesson3.id, teacher1.id)
  }

  it should "throw Exception if lesson id is invalid" in {
    assertThrows[Exception](studentService.getYourLesson(validSession1, invalidTarget).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentUserType(student1.id)
    verify(studentRepoMock, times(1)).yourLesson(invalidTarget, student1.id)
  }

  behavior of "sign up"

  it should "return lesson" in {
    when(userRepoMock.balance(student2.id)).thenReturn(IO.pure(balance1))
    studentService.signUp(validSession2, lesson2.id).unsafeRunSync() shouldBe lesson2
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).studentUserType(student2.id)
    verify(studentRepoMock, times(1)).getLesson(lesson2.id)
    verify(studentRepoMock, times(1)).getLessonsByDate(student2.id, lesson2.date)
    verify(userRepoMock, times(1)).balance(student2.id)
    verify(studentRepoMock, times(1)).reserve(student2.id, lesson2.price)
    verify(studentRepoMock, times(1)).signUp(lesson2.id, student2.id)
  }

  it should "throw Exception if insufficient funds in the account" in {
    when(userRepoMock.balance(student2.id)).thenReturn(IO.pure(balance2))
    assertThrows[Exception](studentService.signUp(validSession2, lesson2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).studentUserType(student2.id)
    verify(studentRepoMock, times(1)).getLesson(lesson2.id)
    verify(studentRepoMock, times(1)).getLessonsByDate(student2.id, lesson2.date)
    verify(userRepoMock, times(1)).balance(student2.id)
    verify(studentRepoMock, times(0)).reserve(student2.id, lesson2.price)
    verify(studentRepoMock, times(0)).signUp(lesson2.id, student2.id)
  }

  it should "throw Exception if user is Busy" in {
    assertThrows[Exception](studentService.signUp(validSession1, lesson2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentUserType(student1.id)
    verify(studentRepoMock, times(1)).getLesson(lesson2.id)
    verify(studentRepoMock, times(1)).getLessonsByDate(student1.id, lesson2.date)
    verify(userRepoMock, times(1)).balance(student1.id)
    verify(studentRepoMock, times(0)).reserve(student1.id, lesson2.price)
    verify(studentRepoMock, times(0)).signUp(lesson1.id, student2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.signUp(invalidSession, lesson2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).studentUserType(student2.id)
    verify(studentRepoMock, times(0)).getLesson(lesson2.id)
    verify(studentRepoMock, times(0)).getLessonsByDate(student2.id, lesson2.date)
    verify(userRepoMock, times(0)).balance(student2.id)
    verify(studentRepoMock, times(0)).reserve(student2.id, lesson2.price)
    verify(studentRepoMock, times(0)).signUp(lesson2.id, student2.id)
  }

  it should "throw Exception if it is another student lesson" in {
    assertThrows[Exception](studentService.signUp(validSession2, lesson3.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).studentUserType(student2.id)
    verify(studentRepoMock, times(1)).getLesson(lesson3.id)
    verify(studentRepoMock, times(0)).getLessonsByDate(student2.id, lesson3.date)
    verify(userRepoMock, times(0)).balance(student2.id)
    verify(studentRepoMock, times(0)).reserve(student2.id, lesson3.price)
    verify(studentRepoMock, times(0)).signUp(lesson3.id, student2.id)
  }

  it should "throw Exception if user is not a Student" in {
    assertThrows[Exception](studentService.signUp(validSession3, lesson2.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession3)
    verify(studentRepoMock, times(1)).studentUserType(teacher1.id)
    verify(studentRepoMock, times(0)).getLesson(lesson2.id)
    verify(studentRepoMock, times(0)).getLessonsByDate(teacher1.id, lesson2.date)
    verify(userRepoMock, times(0)).balance(teacher1.id)
    verify(studentRepoMock, times(0)).reserve(teacher1.id, lesson2.price)
    verify(studentRepoMock, times(0)).signUp(lesson2.id, teacher1.id)
  }

  it should "throw Exception if lesson id is invalid" in {
    assertThrows[Exception](studentService.signUp(validSession1, invalidTarget).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentUserType(student1.id)
    verify(studentRepoMock, times(1)).getLesson(invalidTarget)
    verify(studentRepoMock, times(0)).getLessonsByDate(student1.id, lesson2.date)
    verify(userRepoMock, times(0)).balance(student1.id)
    verify(studentRepoMock, times(0)).reserve(student1.id, lesson2.price)
    verify(studentRepoMock, times(0)).signUp(invalidTarget, student1.id)
  }

  behavior of "sign out"

  it should "sign out" in {
    studentService.signOut(validSession1, lesson3.id).unsafeRunSync() shouldBe 1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentLesson(lesson3.id, student1.id)
    verify(studentRepoMock, times(1)).unreserve(student1.id, lesson3.price)
    verify(studentRepoMock, times(1)).signOut(lesson3.id, student1.id)
  }

  it should "throw Exception if session invalid" in {
    assertThrows[Exception](studentService.signOut(invalidSession, lesson3.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).studentLesson(lesson3.id, student1.id)
    verify(studentRepoMock, times(0)).unreserve(student1.id, lesson3.price)
    verify(studentRepoMock, times(0)).signOut(lesson3.id, student1.id)
  }

  it should "throw Exception if it is not user lesson" in {
    assertThrows[Exception](studentService.signOut(validSession2, lesson3.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).studentLesson(lesson3.id, student2.id)
    verify(studentRepoMock, times(0)).unreserve(student2.id, lesson3.price)
    verify(studentRepoMock, times(0)).signOut(lesson3.id, student2.id)
  }

  it should "throw Exception if lesson id is invalid" in {
    assertThrows[Exception](studentService.signOut(validSession1, invalidTarget).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentLesson(invalidTarget, student1.id)
    verify(studentRepoMock, times(0)).unreserve(student1.id, lesson3.price)
    verify(studentRepoMock, times(0)).signOut(invalidTarget, student1.id)
  }

  behavior of "upcoming lessons"

  it should "return list of upcoming lessons" in {
    studentService.upcomingLessons(validSession2).unsafeRunSync() shouldBe List()
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).upcoming(student2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.upcomingLessons(invalidSession).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).upcoming(student2.id)
  }

  behavior of "next"

  it should "return next lesson" in {
    studentService.nextLesson(validSession1).unsafeRunSync() shouldBe lesson3
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).upcoming(student1.id)
  }

  it should "throw Exception if user doesn't have next lesson " in {
    assertThrows[Exception](studentService.nextLesson(validSession2).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession2)
    verify(studentRepoMock, times(1)).upcoming(student2.id)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.nextLesson(invalidSession).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).upcoming(student2.id)
  }

  behavior of "evaluate teacher"

  it should "evaluate teacher" in {
    studentService.evaluateTeacher(validSession1, GradeReq(teacher1.id, 0)).unsafeRunSync() shouldBe 1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentUserType(student1.id)
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).teacherGrade(teacher1.id)
    verify(studentRepoMock, times(1)).evaluateTeacherUpdate(teacher1.id, 0, 1)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.evaluateTeacher(invalidSession,GradeReq(teacher1.id, 0)).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).studentUserType(student1.id)
    verify(teacherRepoMock, times(0)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(0)).teacherGrade(teacher1.id)
    verify(studentRepoMock, times(0)).evaluateTeacherUpdate(teacher1.id, 0, 1)
  }

  it should "throw Exception if user is not student" in {
    assertThrows[Exception](studentService.evaluateTeacher(validSession3,GradeReq(teacher1.id, 0)).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession3)
    verify(studentRepoMock, times(1)).studentUserType(teacher1.id)
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
    verify(teacherRepoMock, times(1)).teacherGrade(teacher1.id)
    verify(studentRepoMock, times(0)).evaluateTeacherUpdate(teacher1.id, 0, 1)
  }

  it should "throw Exception if target is not teacher" in {
    assertThrows[Exception](studentService.evaluateTeacher(validSession1,GradeReq(student2.id, 0)).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).studentUserType(student1.id)
    verify(teacherRepoMock, times(1)).getTeacher(student2.id)
    verify(teacherRepoMock, times(0)).teacherGrade(student2.id)
    verify(studentRepoMock, times(0)).evaluateTeacherUpdate(student2.id, 0, 1)
  }

  behavior of "cash in"

  it should "cashIn" in {
    studentService.cashIn(validSession1, CashInReq(500)).unsafeRunSync() shouldBe balance1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(userRepoMock, times(1)).cashIn(student1.id, 500)
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.cashIn(invalidSession,CashInReq(500)).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(userRepoMock, times(0)).cashIn(student1.id, 500)
  }

  behavior of "send homework"

  it should "send homework" in {
    studentService.sendHomework(validSession1, HomeworkReq(lesson1.id, student1.id,"hw")).unsafeRunSync() shouldBe lesson1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(1)).homework(lesson1.id, student1.id, "hw")
  }

  it should "throw Exception if session is invalid" in {
    assertThrows[Exception](studentService.sendHomework(invalidSession, HomeworkReq(lesson1.id, student1.id, "hw")).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(studentRepoMock, times(0)).homework(lesson1.id, student1.id, "hw")
  }
  it should "throw Exception if student id is invalid" in {
    assertThrows[Exception](studentService.sendHomework(validSession1, HomeworkReq(lesson1.id, student2.id, "hw")).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession1)
    verify(studentRepoMock, times(0)).homework(lesson1.id, student1.id, "hw")
  }

  behavior of "find teacher"

  it should "return existing teacher" in {
    studentService.findTeacher(teacher1.id).unsafeRunSync() shouldBe teacherDao1.toTeacher
    verify(teacherRepoMock, times(1)).getTeacher(teacher1.id)
  }

  it should "throw Exception if user doesn't exist" in {
    assertThrows[Exception](studentService.findTeacher(invalidTarget).unsafeRunSync())
    verify(teacherRepoMock, times(1)).getTeacher(invalidTarget)
  }
}
