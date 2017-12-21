import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{DeliusPushResult, Job}
import gov.uk.justice.digital.icantbelieveitsnotdelius.services.DeliusTarget
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.scalatest.{BeforeAndAfterAll, _}

import scala.concurrent.Await
import scala.concurrent.duration._

class DeliusSpec extends FunSpec with BeforeAndAfter with BeforeAndAfterAll with GivenWhenThen with Matchers {

  implicit val formats = Serialization.formats(NoTypeHints)
  implicit val system = ActorSystem()
  implicit val materialzer = ActorMaterializer()

  describe("Job sent to Delius API") {

    it("Performs the POST verb with request body and headers on the API") {

      val target = new DeliusTarget(s"http://localhost:$port/delius", "username", "password")

      Given("a Job")

      val uuid = UUID.randomUUID().toString

      val headerMap = Map[String, String]("content-type" -> "application/json",
        "host" -> "delius-api-job-schedular-dev.tqek38d8jq.eu-west-2.elasticbeanstalk.com",
        "x-real-ip" -> "172.31.29.70",
        "x-forwarded-for" -> "82.22.2.33, 172.31.29.70",
        "accept" -> "text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, image/apng, */*;q=0.8",
        "accept-encoding" -> "gzip, deflate",
        "accept-language" -> "en-GB,en-US;q=0.9,en;q=0.8",
        "cache-control" -> "max-age=0",
        "upgrade-insecure-requests" -> "1",
        "user-agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36",
        "x-forwarded-port" -> "80",
        "x-forwarded-proto" -> "http")

      val aJob = Job(uuid, headerMap, "/hello", Some("eyJhIjoiYiJ9"), "POST")

      When("the Job is pushed to the target")
      val result = Await.result(target.push(aJob), 5.seconds)

      Then("the API receives a HTTP POST call")
      verify(
        postRequestedFor(urlEqualTo("/delius/hello")).
          withHeader("Content-type", equalTo("application/json")).
          withHeader("host", equalTo("delius-api-job-schedular-dev.tqek38d8jq.eu-west-2.elasticbeanstalk.com")).
          withHeader("x-real-ip", equalTo("172.31.29.70")).
          withHeader("x-forwarded-for", equalTo("82.22.2.33, 172.31.29.70")).
          withHeader("accept", equalTo("text/html, application/xhtml+xml, application/xml;q=0.9, image/webp, image/apng, */*;q=0.8")).
          withHeader("accept-encoding", equalTo("gzip, deflate")).
          withHeader("accept-language", equalTo("en-GB, en-US;q=0.9, en;q=0.8")).
          withHeader("cache-control", equalTo("max-age=0")).
          withHeader("upgrade-insecure-requests", equalTo("1")).
          withHeader("user-agent", equalTo("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36")).
          withHeader("x-forwarded-port", equalTo("80")).
          withHeader("x-forwarded-proto", equalTo("http")).
          //          withBasicAuth(new BasicCredentials("username", "password")).
          withRequestBody(equalTo("{\"a\":\"b\"}"))
      )

      result.copy(headers = None) shouldBe DeliusPushResult(aJob, Some(StatusCodes.OK), None, Some("Hello!"), None)
    }

    it("Performs the GET verb without request body and no content-type header on the API") {

      //      configureFor(8071)
      //      val api = new WireMockServer(options.port(8071))
      val target = new DeliusTarget(s"http://localhost:$port/delius", "username", "password")
      //      api.start()

      Given("a Job")

      val uuid = UUID.randomUUID().toString

      val headerMap = Map[String, String]()

      val aJob = Job(uuid, headerMap, "/hello", None, "GET")

      When("the Job is pushed to the target")
      val result = Await.result(target.push(aJob), 5.seconds)

      Then("the API receives a HTTP POST call")
      verify(
        getRequestedFor(urlEqualTo("/delius/hello")).
          withoutHeader("Content-type")
      )

      result.copy(headers = None) shouldBe DeliusPushResult(aJob, Some(StatusCodes.OK), None, Some("Hello!"), None)

    }
  }

  val port = 8090
  private var mockedRestAPIs: Option[WireMockServer] = None

  before {
    mockedRestAPIs = Some(new WireMockServer(port))
    configureFor(port)
    mockedRestAPIs.get.start()
  }

  after {
    mockedRestAPIs.get.stop()
  }

  override protected def afterAll(): Unit = {
    system.terminate()
  }
}
