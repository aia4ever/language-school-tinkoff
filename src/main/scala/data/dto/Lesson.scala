package data.dto

import data.dto.Lesson.Insert

import java.time.Instant

case class Lesson(
                   id: Long,
                   teacherId: Long,
                   date: Instant,
                   price: BigDecimal,
                   zoomLink: String,
                   studentId: Option[Long],
                   homework: Option[String],
                   answer: Option[String],
                   mark: Option[Double],
                   purchased: Boolean
                 ){
     lazy val toInsert: Insert = Insert(teacherId, date, price, zoomLink)
}

object Lesson {
  case class Insert (
                      teacherId: Long,
                      date: Instant,
                      price: BigDecimal,
                      zoomLink: String,
                    )
}
