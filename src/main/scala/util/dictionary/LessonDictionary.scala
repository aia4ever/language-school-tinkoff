package util.dictionary

import data.dto.Lesson
import data.req.LessonUpdateReq
import doobie.Fragment
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._
import util.{StudentType, TeacherType, UserType}

import java.time.Instant
import java.time.temporal.ChronoUnit


object LessonDictionary {

  private lazy val cols: Seq[String] =
    Seq("id", "teacher_id", "lesson_date", "price", "zoom_link", "student_id", "homework", "answer", "mark", "is_purchased")

  private lazy val base =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark, is_purchased
         from lesson"""

  private lazy val userTypeMatch: Map[UserType, Fragment] = Map(
    (StudentType, fr" and student_id = "),
    (TeacherType, fr" and teacher_id = ")
  )

  def createLesson(insert: Lesson.Insert): ConnectionIO[Lesson] =
    sql"""insert into lesson(teacher_id, lesson_date, price, zoom_link)
        values (${insert.teacherId}, ${insert.date}, ${insert.price}, ${insert.zoomLink})"""
      .update
      .withUniqueGeneratedKeys[Lesson](cols: _*)

  def updateLesson(lesson: LessonUpdateReq): ConnectionIO[Lesson] =
    sql"""update lesson
        set homework = ${lesson.homework}, zoom_link = ${lesson.zoomLink}, mark = ${lesson.mark}
            where id = ${lesson.id}
       """.update
      .withUniqueGeneratedKeys[Lesson](cols: _*)

  def deleteLesson(lessonId: Long, teacherId: Long): ConnectionIO[Int] =
    sql"delete from lesson where teacher_id = $teacherId and id = $lessonId"
      .update.run

  def getLesson(lessonId: Long, userId: Long, userType: UserType): ConnectionIO[Option[Lesson]] =
    (base ++ fr" where id = $lessonId" ++ userTypeMatch(userType) ++ fr" $userId")
      .query[Lesson].option

  def upcomingLessons(userId: Long, userType: UserType): ConnectionIO[List[Lesson]] =
    (base ++
      fr" where lesson_date >= current_timestamp " ++ userTypeMatch(userType) ++ fr" $userId" ++ fr" order by lesson_date")
      .query[Lesson].stream.compile.toList

  def previousLessons(userId: Long, userType: UserType): ConnectionIO[List[Lesson]] =
    (base ++
      fr" where lesson_date < current_timestamp " ++ userTypeMatch(userType) ++ fr" $userId" ++ fr" order by lesson_date")
      .query[Lesson].stream.compile.toList

  def getEmptyLesson(lessonId: Long): ConnectionIO[Option[Lesson]] =
    (base ++ fr" where id = $lessonId and student_id is null")
      .query[Lesson].option

  def studentLesson(lessonId: Long, studentId: Long): ConnectionIO[Option[Lesson]] =
    (base ++ fr" where id = $lessonId and student_id = $studentId")
      .query[Lesson].option


  def lessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]] =
    (base ++ fr" where teacher_id = $teacherId and student_id is null")
      .query[Lesson].stream.compile.toList

  def signUp(lessonId: Long, studentId: Long): ConnectionIO[Lesson] =
    sql"""
        update lesson
        set student_id = $studentId
            where id = $lessonId
       """
      .update
      .withUniqueGeneratedKeys[Lesson](cols: _*)


  def signOut(lessonId: Long, studentId: Long): ConnectionIO[Int] =
    sql"""
        update lesson
        set student_id = null
            where id = $lessonId and student_id = $studentId
       """
      .update.run



  def homework(lessonId: Long, studentId: Long, homework: String): ConnectionIO[Lesson] =
    sql"""update lesson
        set answer = $homework
            where id = $lessonId
       """.update
      .withUniqueGeneratedKeys[Lesson](cols: _*)

  def evaluateHomework(lessonId: Long, teacherId: Long, mark: Double): ConnectionIO[Lesson] =
    sql"""update lesson
        set mark = $mark
        where id = $lessonId and teacher_id = $teacherId
       """.update
      .withUniqueGeneratedKeys[Lesson](cols: _*)

  def getLessonsByDate(userId: Long, userType: UserType, date: Instant): ConnectionIO[List[Lesson]] =
    (base ++
      fr""" where lesson_date between ${date.minus(1, ChronoUnit.HOURS)}
                and ${date.plus(1, ChronoUnit.HOURS)}""" ++ userTypeMatch(userType) ++ fr"$userId")
      .query[Lesson].stream.compile.toList

}
