package teacher

import data.dao.TeacherDao
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.implicits.legacy.instant._
import lesson.Lesson

object TeacherRepository {


  def getTeacher(teacherId: Long): ConnectionIO[Option[TeacherDao]] =
    sql"""select ut.id, ut.firstname, ut.surname, ut.sex, te.bio, te.average_grade, te.grade_amount
      from user_table ut
          left join teacher_extension te on ut.id = te.teacher_id and ut.user_type = 'Teacher'
      where ut.id = $teacherId
       """.query[TeacherDao].option

  def getAllTeachers: ConnectionIO[List[TeacherDao]] =
    sql"""
      select ut.id, ut.firstname, ut.surname, ut.sex, te.bio, te.average_grade, te.grade_amount
      from user_table ut
          left join teacher_extension te on ut.id = te.teacher_id and ut.user_type = 'Teacher'
       """.query[TeacherDao].stream.compile.toList

  def createLesson(insert: Lesson.Insert): ConnectionIO[Lesson] =
    sql"""insert into lesson(teacher_id, lesson_date, price, zoom_link)
        values (${insert.teacherId}, ${insert.date}, ${insert.price}, ${insert.zoomLink})
       """.update
      .withUniqueGeneratedKeys[Lesson]("id", "teacher_id", "lesson_date", "price", "zoom_link", "student_id", "homework", "answer", "mark")

  def updateLesson(lesson: Lesson, teacherId: Long): ConnectionIO[Lesson] =
    sql"""update lesson
        set lesson_date = $lesson.date,
            price = $lesson.price,
            zoom_link = $lesson.zoomLink,
            homework = $lesson.homework,
            mark = $lesson.mark
        where id = $lesson.id and teacher_id = $teacherId
       """.update
      .withUniqueGeneratedKeys[Lesson]("id", "teacher_id", "date", "zoom_link", "price", "student_id", "homework", "answer", "mark")

  def deleteLesson(lessonId: Long, teacherId: Long): ConnectionIO[Int] =
    sql"delete from lesson where teacher_id = $teacherId and id = $lessonId"
      .update.run

  def getLesson(lessonId: Long, teacherId: Long): ConnectionIO[Option[Lesson]] =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
        where id = $lessonId and teacher_id = $teacherId
       """.query[Lesson].option

  def upcomingLessons(teacherId: Long): ConnectionIO[List[Lesson]] =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
         where teacher_id = $teacherId and lesson_date >=current_timestamp
         order by lesson_date
       """.query[Lesson].stream.compile.toList

  def previousLessons(teacherId: Long): ConnectionIO[List[Lesson]] =
    sql"""select id, teacher_id, lesson_date, price, zoom_link, student_id, homework, answer, mark from lesson
        where teacher_id = $teacherId and lesson_date < current_timestamp
        order by lesson_date
       """.query[Lesson].stream.compile.toList

  def bioUpdate(id: Long, bio: String): ConnectionIO[Int] = {
    sql"""insert into teacher_extension(teacher_id, bio) values ($id, $bio)
            on conflict (teacher_id) do update set bio = $bio
       """.update.run
  }

}
