/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.common.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import helloscala.common.Colors

/**
 * A logback converter generating colored, lower-case level names.
 *
 * Used for example as:
 * {{{
 * %coloredLevel %logger{15} - %message%n%xException{5}
 * }}}
 */
class ColoredLevel extends ClassicConverter {
  def convert(event: ILoggingEvent): String =
    event.getLevel match {
      case Level.TRACE => "[" + Colors.blue("trace") + "]"
      case Level.DEBUG => "[" + Colors.cyan("debug") + "]"
      case Level.INFO  => "[" + Colors.white("info") + "]"
      case Level.WARN  => "[" + Colors.yellow("warn") + "]"
      case Level.ERROR => "[" + Colors.red("error") + "]"
    }
}
