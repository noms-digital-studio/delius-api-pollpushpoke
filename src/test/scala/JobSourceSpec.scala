import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{Job, SchedulerPullResult}
import gov.uk.justice.digital.icantbelieveitsnotdelius.services.JobSource
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.scalatest.{BeforeAndAfterAll, FunSpec, GivenWhenThen, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class JobSourceSpec extends FunSpec with BeforeAndAfterAll with GivenWhenThen with Matchers {

  implicit val formats = Serialization.formats(NoTypeHints)
  implicit val system = ActorSystem()
  implicit val materialzer = ActorMaterializer()

  describe("Pull from job scheduler API") {

    it("GETs job from the API") {

      val testPort = 8093

      configureFor(testPort)
      val api = new WireMockServer(options.port(testPort))
      val source = new JobSource(s"http://localhost:$testPort/job")

      Given("the source API")
      api.start()

      When("Jobs are pulled from the API")
      val result = Await.result(source.pull(), 5.seconds)

      Then("the API receives a HTTP GET and returns the Job")
      verify(
        getRequestedFor(
          urlEqualTo("/job"))
      )

      val expected = SchedulerPullResult(Job("5582cb83-ebd4-4e90-8b8d-f65eae81d79a",
        Map(
          "content-type" -> "application/json",
          "host" -> "delius-api-job-schedular-dev.tqek38d8jq.eu-west-2.elasticbeanstalk.com",
          "x-real-ip" -> "172.31.29.70",
          "x-forwarded-for" -> "82.22.2.33, 172.31.29.70",
          "accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
          "accept-encoding" -> "gzip, deflate",
          "accept-language" -> "en-GB,en-US;q=0.9,en;q=0.8",
          "cache-control" -> "max-age=0",
          "upgrade-insecure-requests" -> "1",
          "user-agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36",
          "x-forwarded-port" -> "80",
          "x-forwarded-proto" -> "http"), "/hello", Some("eyJhIjoiYiJ9"), "POST"), StatusCodes.OK, None)

      result shouldBe expected

      api.stop()
    }


    it("reports a failure HTTP response code as an error") {

      val testPort = 8094

      configureFor(testPort)
      val api = new WireMockServer(options.port(testPort))
      val source = new JobSource(s"http://localhost:$testPort/internalError")

      Given("the source API returns an 500 Internal Error")
      api.start()

      When("a Job pull from the API is attempted")
      val result = Await.result(source.pull(), 5.seconds)

      Then("the 500 error is reported")
      result.error.get.toString should include("500")

      api.stop()
    }
  }

  override def afterAll() {

    system.terminate()
  }
}
