package uk.gov.hmrc.onestopshopforexrates.scheduler

import akka.actor.{Actor, ActorLogging, Props}
import uk.gov.hmrc.onestopshopforexrates.scheduler.SchedulingActor.ScheduledMessage

import scala.concurrent.ExecutionContext.Implicits.global

class SchedulingActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case message: ScheduledMessage[_] => message.service.invoke
  }
}

object SchedulingActor {

  sealed trait ScheduledMessage[A] {
    val service: ScheduledService[A]
  }

  def props: Props = Props[SchedulingActor]

  case class ProcessUploadedFilesClass(service: ProcessUploadedFilesService) extends ScheduledMessage[Boolean]

}

