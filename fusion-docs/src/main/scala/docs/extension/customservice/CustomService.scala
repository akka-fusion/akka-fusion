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

import akka.actor.typed.ActorSystem
import fusion.common.extension.{ TypedExtension, TypedExtensionId }
import helloscala.common.exception.HSUnauthorizedException
import helloscala.common.util.{ DigestUtils, StringUtils }

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
class FileService private (override val typedSystem: ActorSystem[Nothing]) extends TypedExtension {
  def findUrlById(fileId: String): Future[String] = Future.successful {
    s"http://localhost:9999/file/$fileId.png"
  }
}

object FileService extends TypedExtensionId[FileService] {
  override def createExtension(system: ActorSystem[_]): FileService = new FileService(system)
}

class UserService private (override val typedSystem: ActorSystem[Nothing]) extends TypedExtension {
  import typedSystem.executionContext
  private val fileService = FileService(typedSystem)
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

object UserService extends TypedExtensionId[UserService] {
  override def createExtension(system: ActorSystem[_]): UserService = new UserService(system)
}

class LoginService private (override val typedSystem: ActorSystem[Nothing]) extends TypedExtension {
  import typedSystem.executionContext
  private val userService = UserService(typedSystem)

  def login(dto: LoginDTO): Future[LoginBO] = {
    userService.findByAccount(dto.account).map {
      case user if user.password == DigestUtils.sha256Hex(dto.password + user.salt) =>
        LoginBO(user.id, user.nickname)
      case _ =>
        throw HSUnauthorizedException("密码不匹配")
    }
  }
}

object LoginService extends TypedExtensionId[LoginService] {
  override def createExtension(system: ActorSystem[_]): LoginService = new LoginService(system)
}
// #CustomService
