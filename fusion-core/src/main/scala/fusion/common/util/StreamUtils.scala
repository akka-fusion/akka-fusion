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

package fusion.common.util

import java.nio.file.Path

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.{ FileIO, Sink, Source }
import akka.util.ByteString
import helloscala.common.util.DigestUtils.digestSha256
import helloscala.common.util.StringUtils
import org.reactivestreams.Publisher

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

object StreamUtils {
  object AsInt {
    def unapply(bs: ByteString): Option[Int] = Try(bs.utf8String.toInt).toOption
  }
  object AsLong {
    def unapply(bs: ByteString): Option[Long] = Try(bs.utf8String.toLong).toOption
  }
  object AsDouble {
    def unapply(bs: ByteString): Option[Double] = Try(bs.utf8String.toDouble).toOption
  }
  object AsBoolean {
    def unapply(bs: ByteString): Option[Boolean] = Try(bs.utf8String.toBoolean).toOption
  }

  def publishToHead[T](publisher: Publisher[T])(implicit mat: Materializer): Future[T] =
    Source.fromPublisher(publisher).runWith(Sink.head)

  def publishToHeadOption[T](publisher: Publisher[T])(implicit mat: Materializer): Future[Option[T]] =
    Source.fromPublisher(publisher).runWith(Sink.headOption)

  def publishToSeq[T](publisher: Publisher[T])(implicit mat: Materializer): Future[immutable.Seq[T]] =
    Source.fromPublisher(publisher).runWith(Sink.seq)

  def publishToIgnore[T](publisher: Publisher[T])(implicit mat: Materializer): Future[Done] =
    Source.fromPublisher(publisher).runWith(Sink.ignore)

  def sourceToPublish[T](source: Source[T, _])(implicit mat: Materializer): Publisher[T] =
    source.runWith(Sink.asPublisher(false))

  def sourceToPublishMultiple[T](source: Source[T, _])(implicit mat: Materializer): Publisher[T] =
    source.runWith(Sink.asPublisher(true))

  def reactiveSha256Hex(path: Path)(implicit mat: Materializer, ec: ExecutionContext): Future[String] = {
    reactiveSha256(path).map(bytes => StringUtils.hex2Str(bytes))
  }

  def reactiveSha256(path: Path)(implicit mat: Materializer, ec: ExecutionContext): Future[Array[Byte]] = {
    val md = digestSha256()
    FileIO.fromPath(path).map(bytes => md.update(bytes.asByteBuffer)).runWith(Sink.ignore).map(_ => md.digest())
  }
}
