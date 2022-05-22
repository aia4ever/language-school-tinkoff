package data.req

case class HomeworkReq(
                        lessonId: Long,
                        studentId: Long,
                        homework: String
                      )
