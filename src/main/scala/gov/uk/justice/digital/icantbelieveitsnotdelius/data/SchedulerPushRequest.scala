package gov.uk.justice.digital.icantbelieveitsnotdelius.data

case class SchedulerPushRequest(status: Int, headers: Option[Map[String, String]], body: Option[String])

