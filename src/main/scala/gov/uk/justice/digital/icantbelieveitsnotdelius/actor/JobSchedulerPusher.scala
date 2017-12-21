package gov.uk.justice.digital.icantbelieveitsnotdelius.actor

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import com.google.inject.Inject
import gov.uk.justice.digital.icantbelieveitsnotdelius.data._
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.JobResultSingleTarget

import scala.concurrent.ExecutionContext.Implicits.global

class JobSchedulerPusher @Inject()(target: JobResultSingleTarget) extends Actor with ActorLogging {

  override def receive = {

    case deliusResponse@DeliusPushResult(_, _, _, _, _) =>

      log.info(s"Pushing delius result to job scheduler: $deliusResponse ...")
      target.push(deliusResponse).pipeTo(self)

    case pushResult@SchedulerPushResult(_, _, _) =>

      ((pushResult.status, pushResult.error) match {

        case (_, Some(error: RuntimeException)) if error.getMessage.contains("Exceeded configured max-open-requests value of") =>

          log.info(s"Re-pushing job: $pushResult as client connection pool is full")
          target.push(pushResult.deliusPushResult).pipeTo(self)

        case (_, Some(error)) => log.warning(s"$pushResult PUSH ERROR: ${error.getMessage}")
        case (Some(result), None) => log.info(s"Push for ${pushResult.deliusPushResult}) returned ${result.value}")
        case _ => log.warning("PUSH ERROR: No result or error")

      }) match {

        case _ => // A Future is returned by target.push. Apparently. Not sure why this is important though.

      }
  }
}
