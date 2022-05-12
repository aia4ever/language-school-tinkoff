package lesson
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._

import java.time.Instant
import java.time.temporal.ChronoUnit

class DBLessonRepository extends LessonRepository {

  val cols = Seq("id", "teacher_id", "lesson_date", "price", "zoom_link", "student_id", "homework", "answer", "mark")

  def createLesson(insert: Lesson.Insert): ConnectionIO[Lesson] =
    sql"""insert into lesson(teacher_id, lesson_date, price, zoom_link)
        values (${insert.teacherId}, ${insert.date}, ${insert.price}, ${insert.zoomLink})
       """.update
      .withUniqueGeneratedKeys[Lesson](cols:_*)

  def updateLesson(lesson: Lesson, teacherId: Long): ConnectionIO[Lesson] =
    sql"""update lesson
        set lesson_date = $lesson.date,
            price = $lesson.price,
            zoom_link = $lesson.zoomLink,
            homework = $lesson.homework,
            mark = $lesson.mark
        where id = $lesson.id and teacher_id = $teacherId
       """.update
      .withUniqueGeneratedKeys[Lesson](cols:_*)

  def deleteLesson(lessonId: Long, teacherId: Long): ConnectionIO[Int] =
    sql"delete from lesson where teacher_id = $teacherId and id = $lessonId"
      .update.run

  def teacherLesson(lessonId: Long, teacherId: Long): ConnectionIO[Option[Lesson]] =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
        where id = $lessonId and teacher_id = $teacherId
       """.query[Lesson].option

  def upcomingLessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]] =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where teacher_id = $teacherId and lesson_date >=current_timestamp
         order by lesson_date
       """.query[Lesson].stream.compile.toList

  def previousLessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]] =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
        where teacher_id = $teacherId and lesson_date < current_timestamp
        order by lesson_date
       """.query[Lesson].stream.compile.toList

   def emptyLesson(lessonId: Long): ConnectionIO[Option[Lesson]] =
     sql"""
        select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark  from lesson
        where id = $lessonId and student_id is null
       """
       .query[Lesson].option

   def studentLesson(lessonId: Long, studentId: Long): ConnectionIO[Option[Lesson]] =
     sql"""
        select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark  from lesson
        where id = $lessonId and student_id = $studentId
       """
       .query[Lesson].option

  def previousLessonsByStudent(id: Long): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where student_id = $id and lesson_date < current_timestamp
         order by lesson_date
       """
      .query[Lesson].stream.compile.toList

  def lessonsByTeacher(teacherId: Long): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
            where teacher_id = $teacherId and student_id is null
       """
      .query[Lesson].stream.compile.toList

  def signUp(lessonId: Long, studentId: Long): ConnectionIO[Lesson] =
    sql"""
        update lesson
        set student_id = $studentId
        where id = $lessonId
       """
      .update
      .withUniqueGeneratedKeys[Lesson](cols:_*)


  def signOut(lessonId: Long, studentId: Long): ConnectionIO[Int] =
    sql"""
        update lesson
        set student_id = null
        where id = $lessonId and student_id = $studentId
       """
      .update.run

  def upcomingLessonsByStudent(studentId: Long): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where student_id = $studentId and lesson_date >=current_timestamp
         order by lesson_date
       """
      .query[Lesson].stream.compile.toList

  def homework(lessonId: Long, studentId: Long, homework: String): ConnectionIO[Lesson] =
    sql"""update lesson
        set homework = $homework
        where id = $lessonId and student_id = $studentId
       """.update
      .withUniqueGeneratedKeys[Lesson](cols:_*)

  def evaluateHomework(lessonId: Long, teacherId: Long, mark: Double): ConnectionIO[Lesson] =
    sql"""update lesson
        set mark = $mark
        where id = $lessonId and teacher_id = $teacherId
       """.update
      .withUniqueGeneratedKeys[Lesson](cols:_*)

  override def teacherLessonsByDate(teacherId: Long, date: Instant): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where teacher_id = $teacherId and
             lesson_date between ${date.minus(1, ChronoUnit.HOURS)} and ${date.plus(1, ChronoUnit.HOURS)}
       """.query[Lesson].stream.compile.toList

  override def studentLessonsByDate(studentId: Long, date: Instant): ConnectionIO[List[Lesson]] =
    sql"""
         select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where teacher_id = $studentId and
             lesson_date between ${date.minus(1, ChronoUnit.HOURS)} and ${date.plus(1, ChronoUnit.HOURS)}
       """.query[Lesson].stream.compile.toList
}


