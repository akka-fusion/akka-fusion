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

///*
// * Copyright 2019 helloscala.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fusion.scheduler.service
//
//import akka.Done
//import akka.actor.Actor
//import akka.actor.ActorLogging
//import akka.actor.Props
//import fusion.scheduler.model.End
//import fusion.job.FusionJob
//import fusion.job.FusionScheduler
//import fusion.scheduler.model._
//import akka.actor.typed.scaladsl.adapter._
//
//class SchedulerActor extends Actor with SchedulerServiceComponent with ActorLogging {
//
//  override def preStart(): Unit = {
//    super.preStart()
//    context.become(onMessage(FusionJob(context.system.toTyped).component))
//    log.info("Scheduler actor startup.")
//  }
//
//  override def postStop(): Unit = {
//    super.postStop()
//    log.info("Scheduler actor stopped.")
//  }
//
//  override def receive: Receive = {
//    case End =>
//      sender() ! Done
//      context.stop(self)
//    case other =>
//      log.warning(s"Scheduler actor not startup, receive message is $other")
//      sender() ! Done
//  }
//
//  private def onMessage(implicit scheduler: FusionScheduler): Receive = {
//    case dto: JobCancelDTO => sender() ! cancelJob(dto)
//    case dto: JobDTO       => sender() ! createJob(dto)
//    case dto: JobGetDTO    => sender() ! getJob(dto)
//    case dto: JobPauseDTO  => sender() ! pauseJob(dto)
//    case dto: JobResumeDTO => sender() ! resumeJob(dto)
//    case End =>
//      scheduler.close()
//      sender() ! Done
//      context.stop(self)
//  }
//
//}
//
//object SchedulerActor {
//  def props(): Props = Props(new SchedulerActor)
//}
