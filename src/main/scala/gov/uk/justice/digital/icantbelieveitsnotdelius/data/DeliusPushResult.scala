package gov.uk.justice.digital.icantbelieveitsnotdelius.data

import akka.http.scaladsl.model.{HttpHeader, StatusCode}

case class DeliusPushResult(job: Job, status: Option[StatusCode], headers: Option[Seq[HttpHeader]], body: Option[String], error: Option[Throwable])
