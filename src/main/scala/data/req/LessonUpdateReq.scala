package data.req

case class LessonUpdateReq(
                          id: Long,
                          zoomLink: String,
                          homework: String,
                          mark: BigDecimal
                          )
