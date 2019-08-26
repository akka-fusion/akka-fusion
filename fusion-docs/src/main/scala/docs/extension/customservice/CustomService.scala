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

package docs.extension.customservice

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionExtension
import helloscala.common.exception.HSUnauthorizedException
import helloscala.common.util.DigestUtils
import helloscala.common.util.StringUtils

import scala.concurrent.Future

case class LoginDTO(account: String, password: String)
case class LoginBO(id: String, nickname: String)
case class UserBO(id: String, nickname: String, avatarId: String, avatarUrl: String)
case class UserDO(id: String, nickname: String, avatarId: String, password: String, salt: String)

class UserRepository {

  def findByAccount(account: String): Future[UserDO] =
    Future.successful(UserDO(StringUtils.randomString(24), account, StringUtils.randomString(24), "password", "salt"))

  def findById(id: String): Future[UserDO] =
    Future.successful(UserDO(id, "用户", StringUtils.randomString(24), "password", "salt"))
}

// #CustomService
class FileService private (val _system: ExtendedActorSystem) extends FusionExtension {

  def findUrlById(fileId: String): Future[String] = Future.successful {
    s"http://localhost:9999/file/$fileId.png"
  }
}

object FileService extends ExtensionId[FileService] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FileService = new FileService(system)
  override def lookup(): ExtensionId[_ <: Extension] = FileService
}

class UserService private (val _system: ExtendedActorSystem) extends FusionExtension {
  import system.dispatcher
  private val fileService = FileService(system)
  private val userRepository = new UserRepository()

  def findBOById(id: String): Future[UserBO] = {
    userRepository.findById(id).flatMap { user =>
      fileService.findUrlById(user.avatarId).map { url =>
        UserBO(user.id, user.nickname, user.avatarId, url)
      }
    }
  }

  def findByAccount(account: String): Future[UserDO] = {
    userRepository.findByAccount(account)
  }
}

object UserService extends ExtensionId[UserService] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): UserService = new UserService(system)
  override def lookup(): ExtensionId[_ <: Extension] = UserService
}

class LoginService private (val _system: ExtendedActorSystem) extends FusionExtension {
  import system.dispatcher
  private val userService = UserService(system)

  def login(dto: LoginDTO): Future[LoginBO] = {
    userService.findByAccount(dto.account).map {
      case user if user.password == DigestUtils.sha256Hex(dto.password + user.salt) =>
        LoginBO(user.id, user.nickname)
      case _ =>
        throw HSUnauthorizedException("密码不匹配")
    }
  }
}

object LoginService extends ExtensionId[LoginService] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): LoginService = new LoginService(system)
  override def lookup(): ExtensionId[_ <: Extension] = LoginService
}
// #CustomService
