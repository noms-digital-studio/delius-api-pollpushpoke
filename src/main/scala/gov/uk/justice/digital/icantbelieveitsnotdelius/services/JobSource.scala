package gov.uk.justice.digital.icantbelieveitsnotdelius.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import com.google.inject.name.Named
import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{Job, SchedulerPullResult, SchedulerPushRequest}
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.SingleSource
import grizzled.slf4j.Logging
import org.json4s.Formats
import org.json4s.native.Serialization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JobSource @Inject()(@Named("sourceUrl") sourceUrl: String)
                         (implicit val formats: Formats,
                          implicit val system: ActorSystem,
                          implicit val materializer: ActorMaterializer) extends SingleSource with Logging {

  private val http = Http()

  private implicit val unmarshaller = Unmarshaller.stringUnmarshaller.forContentTypes(MediaTypes.`application/json`).map { json =>

    logger.debug(s"Received from Nomis: $json")

    read[Job](json)
  }

  override def pull() = {

    logger.info(s"Requesting from job scheduler: $sourceUrl")

    http.singleRequest(
      HttpRequest(
        HttpMethods.GET,
        Uri(sourceUrl)))
      .flatMap {

        case HttpResponse(statusCode, _, _, _) if statusCode.isFailure =>

          throw new Exception(statusCode.value)

        case HttpResponse(StatusCodes.OK, _, entity, _) =>

          Unmarshal(entity).to[Job].map(SchedulerPullResult(_, StatusCodes.OK, None))

        case HttpResponse(statusCode, _, entity, _) =>

          Future.successful(SchedulerPullResult(null, statusCode, None))

      }.recover { case error: Throwable => SchedulerPullResult(null, null, Some(error)) }
  }
}
