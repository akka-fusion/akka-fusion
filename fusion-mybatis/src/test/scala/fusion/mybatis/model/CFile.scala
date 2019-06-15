package fusion.mybatis.model

import java.time.LocalDateTime

import scala.beans.BeanProperty

// #CFile
case class CFile(
    @BeanProperty var fileId: String = "",
    @BeanProperty var fileSubject: String = "",
    @BeanProperty var fileUrl: String = "",
    @BeanProperty var duration: Int = 0,
    @BeanProperty var tableAutoUptime: LocalDateTime = LocalDateTime.now())
// #CFile
