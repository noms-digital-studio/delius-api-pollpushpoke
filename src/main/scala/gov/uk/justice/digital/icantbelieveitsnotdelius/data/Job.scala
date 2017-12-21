package gov.uk.justice.digital.icantbelieveitsnotdelius.data

case class Job(reqId: String, headers: Map[String, String], url: String, body: Option[String], method: String) {}
