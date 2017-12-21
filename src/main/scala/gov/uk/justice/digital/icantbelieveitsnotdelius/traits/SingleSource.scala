package gov.uk.justice.digital.icantbelieveitsnotdelius.traits

import gov.uk.justice.digital.icantbelieveitsnotdelius.data.SchedulerPullResult

import scala.concurrent.Future

trait SingleSource {

  def pull(): Future[SchedulerPullResult]
}
