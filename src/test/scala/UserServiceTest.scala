import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.dao.UserDao
import data.dto.User
import data.req.LoginReq
import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import session.SessionRepository
import user.{UserRepository, UserService}
import util.{Male, StudentType}

class UserServiceTest extends AnyFlatSpec with MockitoSugar with BeforeAndAfterEach {


  val userDao: UserDao = UserDao(0, "test", "test", "test", "test", "test", "test", Male.toString, StudentType.toString)
  val user: User = userDao.toUser
  val user2: User = userDao.toUser.copy(1, "test1")
  val insert2: User.Insert = user2.toInsert
  val insert: User.Insert = user.toInsert

  val validSession = "valid session"
  val invalidSession = "invalid session"

  val invalidId = 1000L

  var userRepoMock: UserRepository = mock[UserRepository]

  var sessionRepoMock: SessionRepository = mock[SessionRepository]

  var userService = new UserService(userRepository = userRepoMock, sessionRepository = sessionRepoMock)

  override def beforeEach(): Unit = {
    userRepoMock = mock[UserRepository]

    sessionRepoMock = mock[SessionRepository]

    userService = new UserService(userRepository = userRepoMock, sessionRepository = sessionRepoMock)

    when(userRepoMock.createUser(insert)).thenReturn(IO.pure(userDao))
    when(userRepoMock.createUser(insert2)).thenThrow(new Exception("User already exists"))

    when(userRepoMock.findByLoginNonBlocked(any)).thenReturn(IO.pure(Option.empty[UserDao]))
    when(userRepoMock.findByLoginNonBlocked(LoginReq(user.login, user.password))).thenReturn(IO.pure(Some(userDao)))


    when(userRepoMock.login(any)).thenReturn(IO.pure(validSession))

    when(sessionRepoMock.getIdBySession(validSession)).thenReturn(IO.pure(Some(user.id)))
    when(sessionRepoMock.getIdBySession(invalidSession)).thenReturn(IO.pure(Option.empty[Long]))

    when(userRepoMock.deleteById(user.id)).thenReturn(IO.pure(1))
    when(userRepoMock.deleteById(invalidId)).thenReturn(IO.pure(0))

    when(userRepoMock.getUserById(user.id)).thenReturn(IO.pure(Some(userDao)))
  }

  behavior of "create"

  it should "create user if user doesn't exist" in {
    userService.create(insert).unsafeRunSync() shouldBe user
    verify(userRepoMock, times(1)).createUser(insert)
  }

  it should "throw Exception if user already exists" in {
    assertThrows[Exception](userService.create(insert2).unsafeRunSync())
    verify(userRepoMock, times(1)).createUser(insert2)
  }

  behavior of "login"

  it should "login correct user" in {
    userService.login(LoginReq(user.login, user.password)).unsafeRunSync() shouldBe validSession
    verify(userRepoMock, times(1)).findByLoginNonBlocked(LoginReq(user.login, user.password))
    verify(userRepoMock, times(1)).login(user)
  }

  it should "throw Exception with invalid password" in {
    assertThrows[Exception](userService.login(LoginReq(user.login, "invalid pw")).unsafeRunSync())
    verify(userRepoMock, times(1)).findByLoginNonBlocked(LoginReq(user.login, "invalid pw"))
    verify(userRepoMock, times(0)).login(user)
  }

  it should "throw Exception with invalid login" in {
    assertThrows[Exception](userService.login(LoginReq(user2.login, user.password)).unsafeRunSync())
    verify(userRepoMock, times(1)).findByLoginNonBlocked(LoginReq(user2.login,  user.password))
    verify(userRepoMock, times(0)).login(user)
  }

  behavior of "delete"

  it should "delete valid user" in {
    userService.delete(validSession, user.id).unsafeRunSync() shouldBe 1
    verify(sessionRepoMock, times(1)).getIdBySession(validSession)
    verify(userRepoMock, times(1)).deleteById(user.id)
  }

  it should "throw Exception with invalid session on delete" in {
    assertThrows[Exception](userService.delete(invalidSession, user.id).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(userRepoMock, times(0)).deleteById(user.id)
  }

  it should "throw Exception with invalid user id" in {
    assertThrows[Exception](userService.delete(validSession, 1000).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(validSession)
    verify(userRepoMock, times(0)).deleteById(1000)
  }

  behavior of "get user by id"

  it should "get valid user" in {
    userService.getUser(validSession).unsafeRunSync() shouldBe user
    verify(sessionRepoMock, times(1)).getIdBySession(validSession)
    verify(userRepoMock, times(1)).getUserById(user.id)
  }

  it should "throw Exception with invalid session on get" in {
    assertThrows[Exception](userService.getUser(invalidSession).unsafeRunSync())
    verify(sessionRepoMock, times(1)).getIdBySession(invalidSession)
    verify(userRepoMock, times(0)).getUserById(user.id)
  }
}
