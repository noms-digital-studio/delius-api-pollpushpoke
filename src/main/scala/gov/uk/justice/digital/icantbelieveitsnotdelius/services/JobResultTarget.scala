package gov.uk.justice.digital.icantbelieveitsnotdelius.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import com.google.inject.name.Named
import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{DeliusPushResult, SchedulerPushRequest, SchedulerPushResult}
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.JobResultSingleTarget
import grizzled.slf4j.Logging
import org.json4s.Formats
import org.json4s.native.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global

class JobResultTarget @Inject()(@Named("jobTargetUrl") targetUrl: String)
                               (implicit val formats: Formats,
                                implicit val system: ActorSystem,
                                implicit val materializer: ActorMaterializer) extends JobResultSingleTarget with Logging {

  private val http = Http()

  def asMap(maybeHeaders: Option[Seq[HttpHeader]]): Option[Map[String, String]] = maybeHeaders.map(
    headers => headers.map(header => header.name() -> header.value()).toMap)

  def asSchedulerPushRequest(deliusResult: DeliusPushResult): SchedulerPushRequest =
    SchedulerPushRequest(deliusResult.status.get.intValue(), asMap(deliusResult.headers), deliusResult.body)

  override def push(deliusResult: DeliusPushResult) = {

    val resource = s"$targetUrl/${deliusResult.job.reqId}"

    logger.debug(s"About to POST delius api result to ${resource} with body ${deliusResult.job.body}")

    val request = HttpRequest()
      .withMethod(HttpMethods.POST)
      .withUri(Uri(resource))
      .withEntity(HttpEntity(MediaTypes.`application/json`, write(asSchedulerPushRequest(deliusResult))))

    http.singleRequest(request).flatMap { response =>

      for (_ <- Unmarshal(response.entity).to[String])
        yield SchedulerPushResult(deliusResult, Some(response.status), None)

    }.recover { case t: Throwable => SchedulerPushResult(deliusResult, None, Some(t)) }
  }
}
