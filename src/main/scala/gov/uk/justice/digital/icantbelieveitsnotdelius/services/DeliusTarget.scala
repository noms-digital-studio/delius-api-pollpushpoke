package gov.uk.justice.digital.icantbelieveitsnotdelius.services

import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import com.google.inject.name.Named
import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{DeliusPushResult, Job}
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.DeliusSingleTarget
import grizzled.slf4j.Logging
import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global

class DeliusTarget @Inject()(@Named("targetUrl") targetUrl: String,
                             @Named("username") username: String,
                             @Named("password") password: String)
                            (implicit val formats: Formats,
                             implicit val system: ActorSystem,
                             implicit val materializer: ActorMaterializer) extends DeliusSingleTarget with Logging {

  private val http = Http()

  def asHttpHeaders(headers: Map[String, String]): List[HttpHeader] =

  // exclude content-type as akka http bitches
    headers.filterKeys(key => !key.equalsIgnoreCase("content-type")).map { case (k, v) => HttpHeader.parse(k, v) match {
      case valid: ParsingResult.Ok => valid.header
    }
    }.toList

  def contentTypeOf(headers: Map[String, String]): ContentType = headers.filterKeys(k => k.equalsIgnoreCase("content-type")).map {
    case (_, v) => ContentType.parse(v)
  }.head match {
    case Right(x) => x
    case Left(x) => ContentTypes.`text/plain(UTF-8)`
  }


  override def push(job: Job) = {

    val verb: HttpMethod = job.method match {
      case "GET" => HttpMethods.GET
      case "POST" => HttpMethods.POST
      case "PUT" => HttpMethods.PUT
      case "DELETE" => HttpMethods.DELETE
    }

    val resource = s"$targetUrl${job.url}"

    val maybeDecodedBody: Option[Array[Byte]] = job.body.map(Base64.getDecoder().decode)

    logger.info(s"About to $verb to $resource with body $maybeDecodedBody")

    val request = HttpRequest()
      .withMethod(verb)
      .withUri(resource)
      .withHeaders(asHttpHeaders(job.headers))

    val self = maybeDecodedBody.map(decodedBody => request.withEntity(contentTypeOf(job.headers), decodedBody)).getOrElse(request)

    http.singleRequest(self)
      .flatMap { response =>

        for (body <- Unmarshal(response.entity).to[String])
          yield DeliusPushResult(job, Some(response.status), Some(response.headers), Some(body), None)

      }.recover { case t: Throwable => DeliusPushResult(job, None, None, Some(""), Some(t)) }
  }
}
