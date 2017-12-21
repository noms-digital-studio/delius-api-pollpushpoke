package gov.uk.justice.digital.icantbelieveitsnotdelius.actor

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.google.inject.Inject
import gov.uk.justice.digital.icantbelieveitsnotdelius.data._
import gov.uk.justice.digital.icantbelieveitsnotdelius.services.DeliusTarget

import scala.concurrent.ExecutionContext.Implicits.global

class DeliusPusher @Inject()(target: DeliusTarget) extends Actor with ActorLogging {

  private def jobSchedulerPusher = context.actorSelection("/user/JobSchedulerPusher")

  override def receive = {

    case jobRequest@Job(_, _, _, _, _) =>

      log.info(s"Pulled new job request: $jobRequest ...")
      target.push(jobRequest).pipeTo(self)

    case pushResult@DeliusPushResult(job, _, _, body, _) =>

      ((pushResult.status, pushResult.error) match {

        case (_, Some(error: RuntimeException)) if error.getMessage.contains("Exceeded configured max-open-requests value of") =>

          log.info(s"Re-pushing job: $job as client connection pool is full")
          target.push(job).pipeTo(self)

        case (_, Some(error)) => log.warning(s"$job PUSH ERROR: ${error.getMessage}")
        case (Some(result), None) => {
          log.info(s"Push for $job returned ${result.value} $body")
          jobSchedulerPusher ! pushResult
        }
        case _ => log.warning("PUSH ERROR: No result or error")

      }) match {

        case _ => // A Future is returned by target.push. Apparently. Not sure why this is important though.

      }
  }
}
