package gov.uk.justice.digital.icantbelieveitsnotdelius.data

import akka.http.scaladsl.model.StatusCode

case class SchedulerPushResult(deliusPushResult: DeliusPushResult, status: Option[StatusCode], error: Option[Throwable])
