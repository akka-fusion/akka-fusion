/*
 * Copyright 2019 akka-fusion.com
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

package fusion.inject.guice

import akka.actor.ExtendedActorSystem
import fusion.common.config.FusionConfigFactory
import fusion.common.constant.FusionConstants
import fusion.core.util.FusionUtils
import fusion.core.{ FusionApplication, FusionApplicationLoader }
import helloscala.common.Configuration

class GuiceApplicationLoader extends FusionApplicationLoader {
  override def load(context: FusionApplicationLoader.Context): FusionApplication = {
    val configuration = Configuration(
      FusionConfigFactory.arrangeConfig(Configuration.fromDiscovery().underlying, FusionConstants.FUSION))
    val system = FusionUtils.createActorSystem(configuration).asInstanceOf[ExtendedActorSystem]
    val injector = new FusionInjector(configuration, system)
    val application = new GuiceApplication(injector)
    if (configuration.getOrElse(FusionConstants.GLOBAL_APPLICATION_ENABLE, false)) {
      FusionApplication.setApplication(application)
    }
    application
  }
}
