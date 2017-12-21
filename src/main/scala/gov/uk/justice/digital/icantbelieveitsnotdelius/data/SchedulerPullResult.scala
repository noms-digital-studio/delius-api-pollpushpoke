package gov.uk.justice.digital.icantbelieveitsnotdelius.data

import akka.http.scaladsl.model.StatusCode

case class SchedulerPullResult(job: Job, status: StatusCode, error: Option[Throwable])
