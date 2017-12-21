package gov.uk.justice.digital.icantbelieveitsnotdelius.traits

import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{DeliusPushResult, SchedulerPushResult}

import scala.concurrent.Future

trait JobResultSingleTarget {

  def push(deliusPushResult: DeliusPushResult): Future[SchedulerPushResult]
}
