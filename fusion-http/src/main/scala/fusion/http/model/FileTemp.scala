package fusion.http.model

import java.nio.file.Path

case class FileTemp(hash: String, contentLength: Long, tmpPath: Path)
