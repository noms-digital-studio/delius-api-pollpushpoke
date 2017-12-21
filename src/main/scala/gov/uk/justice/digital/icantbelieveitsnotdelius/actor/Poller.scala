package gov.uk.justice.digital.icantbelieveitsnotdelius.actor

import akka.actor.{Actor, ActorLogging}
import akka.http.javadsl.model.StatusCodes
import akka.pattern.pipe
import com.google.inject.Inject
import com.google.inject.name.Named
import gov.uk.justice.digital.icantbelieveitsnotdelius.data._
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.SingleSource

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Poller @Inject()(source: SingleSource, @Named("timeout") timeout: Int) extends Actor with ActorLogging {

  log.info(s"Poller created to pull every $timeout seconds ...")

  private case class PullRequest()

  private val duration = timeout.seconds

  private def pusher = context.actorSelection("/user/DeliusPusher")

  override def preStart = self ! PullRequest()

  override def receive = {

    case PullRequest() =>

      log.info(s"Pulling job...")
      source.pull().pipeTo(self)

    case pullResult@SchedulerPullResult(job, _, _) =>

      context.system.scheduler.scheduleOnce(duration, self, PullRequest())

      (pullResult.status, pullResult.error) match {

        case (_, Some(error)) =>

          log.warning(s"PULL ERROR: ${error.getMessage}")

        case (StatusCodes.NO_CONTENT, _) => {
          log.info("Pulled empty job, nothing to do.")
        }

        case (StatusCodes.OK, _) => {
          log.info(s"Pulled job $job")

          pusher ! job
        }

        case (code, None) => {
          log.info(s"Pulled job, unexpected status code $code")
        }

      }
  }
}
