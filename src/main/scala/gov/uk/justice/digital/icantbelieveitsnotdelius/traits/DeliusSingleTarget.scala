package gov.uk.justice.digital.icantbelieveitsnotdelius.traits

import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{DeliusPushResult, Job}

import scala.concurrent.Future

trait DeliusSingleTarget {

  def push(jobRequest: Job): Future[DeliusPushResult]
}
