import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.dto.User
import data.req.LoginReq
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import session.SessionRepository
import user.{UserRepository, UserService}
import util.ApiErrors._
import util.{Male, StudentType}

class UserServiceTest extends AnyFlatSpec with MockitoSugar{
  
  val userRepoMock: UserRepository = mock[UserRepository]
  
  val sessionRepoMock: SessionRepository = mock[SessionRepository]
  
  val userService = new UserService(userRepository = userRepoMock, sessionRepository = sessionRepoMock)

  val user = User(0, "test","test","test","test","test","test", Male, StudentType)
  val insert: User.Insert = user.toInsert

//  behavior of "create"

  it should "create user if user doesn't exist" in {

    when(userRepoMock.createUser(insert)).thenReturn(IO.pure(user))
    userService.create(insert).unsafeRunSync() shouldBe user
  }

  it should "throw Exception if user already exists" in {
    when(userRepoMock.createUser(any[User.Insert])).thenThrow(new Exception("User already exists"))

    assertThrows[Exception](userService.create(insert).unsafeRunSync())
  }

//  behavior of "login"

  it should "login correct user" in {
    when(userRepoMock.findByLoginNonBlocked(any)).thenReturn(IO.pure(user))
    when(userRepoMock.login(user)).thenReturn(IO.pure("TestSession"))
    userService.login(LoginReq(user.login,user.password)).unsafeRunSync() shouldBe "TestSession"
  }

  it should "throw Exception with invalid password" in {
    when(userRepoMock.findByLoginNonBlocked(any)).thenReturn(IO.pure(user))
    when(userRepoMock.login(any)).thenReturn(IO.pure("TestSession"))
    assertThrows[Exception](userService.login(LoginReq(user.login, "invalid pw")).unsafeRunSync())
  }

  it should "throw Exception with invalid login" in {
    when(userRepoMock.findByLoginNonBlocked(any)).thenThrow(InvalidLoginPasswordError)
    when(userRepoMock.login(any)).thenReturn(IO.pure("TestSession"))
    assertThrows[Exception](userService.login(LoginReq("invalid user", user.password)).unsafeRunSync())
  }

//  behavior of "delete"

  it should "delete valid user" in {
    when(sessionRepoMock.getIdBySession("valid session")).thenReturn(IO.pure(user.id))
    when(userRepoMock.deleteById(any)).thenReturn(IO.pure(1))
    userService.delete("valid session", user.id).unsafeRunSync() shouldBe 1
  }

  it should "throw Exception with invalid session on delete" in {
    when(sessionRepoMock.getIdBySession("invalid session")).thenThrow(InvalidSessionError)
    when(userRepoMock.deleteById(any)).thenReturn(IO.pure(1))
    assertThrows[Exception](userService.delete("invalid session", user.id).unsafeRunSync())
  }

  it should "throw Exception with invalid user id" in {
    when(sessionRepoMock.getIdBySession("valid session")).thenReturn(IO.pure(user.id))
    when(userRepoMock.deleteById(any)).thenReturn(IO.pure(1))
    assertThrows[Exception](userService.delete("valid session", 1000).unsafeRunSync())
  }

//  behavior of "get user by id"

  it should "get valid user" in {
    when(sessionRepoMock.getIdBySession("valid session")).thenReturn(IO.pure(user.id))
    when(userRepoMock.getUserById(user.id)).thenReturn(IO.pure(user))
    userService.getUser("valid session").unsafeRunSync() shouldBe user
  }

  it should "throw Exception with invalid session on get" in {
    when(sessionRepoMock.getIdBySession("invalid session1")).thenThrow(InvalidSessionError)
    when(userRepoMock.getUserById(any)).thenReturn(IO.pure(user))
    assertThrows[Exception](userService.getUser("invalid session1").unsafeRunSync())
  }
}
