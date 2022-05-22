package util

import scala.util.control.NoStackTrace

object ApiErrors {

  object InvalidSessionError extends Exception("Invalid session") with NoStackTrace

  object InvalidLoginPasswordError extends Exception("Invalid login/password") with NoStackTrace

  object InsufficientFundsError extends Exception("insufficient funds in the account") with NoStackTrace

  object UserNotFoundError extends Exception("User not found") with NoStackTrace

  object LessonNotFoundError extends Exception("Lesson not found") with NoStackTrace

  object AccessDeniedError extends Exception("Access denied") with NoStackTrace

  object YouAreNotAStudentError extends Exception("You are not a Student") with NoStackTrace

  object YouAreNotATeachError extends Exception("You are not a Teacher") with NoStackTrace

  object SomethingWentWrongError extends Exception("Something went wrong") with NoStackTrace

  object BusyError extends Exception("You have another activity at this time") with NoStackTrace

  object NoLessonError extends Exception("You don't have next lesson") with NoStackTrace

  object NoSessionError extends Exception("No session") with NoStackTrace
  
}
