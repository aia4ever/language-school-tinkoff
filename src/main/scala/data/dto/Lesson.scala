package data.dto

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
                 )

object Lesson {
  case class Insert (
                      teacherId: Long,
                      date: Instant,
                      price: BigDecimal,
                      zoomLink: String,
                    )
}
