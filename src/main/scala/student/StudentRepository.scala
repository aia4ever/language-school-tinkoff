package student

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._
import lesson.Lesson

object StudentRepository {

  def isStudent(studentId: Long): ConnectionIO[Option[String]] =
    sql"""
         select user_type from user_table
            where id = $studentId
       """.query[String].option

  def teacherGrade(teacherId: Long): ConnectionIO[Option[(Double, Int)]] =
    sql"""
        select average_grade, grade_amount from teacher_extension
        where teacher_id = $teacherId
       """
      .query[(Double, Int)].option

  def evaluateTeacherUpdate(teacherId: Long)(newGrade: Double, newAmount: Int): ConnectionIO[Int] =
    sql"""
        insert into teacher_extension (teacher_id, average_grade, grade_amount)
        values ($teacherId, $newGrade, $newAmount)
        on conflict (teacher_id) do update
        set average_grade = $newGrade,
            grade_amount = $newAmount
         """
      .update.run

  def upcomingLessons(studentId: Long): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where student_id = $studentId and lesson_date >=current_timestamp
         order by lesson_date
       """
      .query[Lesson].stream.compile.toList


  def previousLessons(studentId: Long): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where student_id = $studentId and lesson_date < current_timestamp
         order by lesson_date
       """
      .query[Lesson].stream.compile.toList

  def emptyLesson(lessonId: Long): ConnectionIO[Option[Lesson]] =
    sql"""
        select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark  from lesson
        where id = $lessonId and student_id is null
       """
      .query[Lesson].option

  def yourLesson(lessonId: Long, studentId: Long): ConnectionIO[Option[Lesson]] =
    sql"""
        select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark  from lesson
        where id = $lessonId and student_id = $studentId
       """
      .query[Lesson].option


  def signUp(lessonId: Long, studentId: Long): ConnectionIO[Lesson] =
    sql"""
        update lesson
        set student_id = $studentId
        where id = $lessonId
       """
      .update
      .withUniqueGeneratedKeys[Lesson]("id", "teacher_id", "date", "zoom_link", "price", "student_id", "homework", "answer", "mark")

  def signOut(lessonId: Long, studentId: Long): ConnectionIO[Int] =
    sql"""
        update lesson
        set student_id = null
        where id = $lessonId and student_id = $studentId
       """
      .update.run

  def lessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
            where teacher_id = $teacherId and student_id is null
       """
      .query[Lesson].stream.compile.toList
}
