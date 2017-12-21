import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import gov.uk.justice.digital.icantbelieveitsnotdelius.data.{DeliusPushResult, Job}
import gov.uk.justice.digital.icantbelieveitsnotdelius.services.JobResultTarget
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.scalatest.{BeforeAndAfterAll, FunSpec, GivenWhenThen, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class JobResultsSpec extends FunSpec with BeforeAndAfterAll with GivenWhenThen with Matchers {

  implicit val formats = Serialization.formats(NoTypeHints)
  implicit val system = ActorSystem()
  implicit val materialzer = ActorMaterializer()

  describe("Push to job scheduler API") {

    it("POSTs job result to the API") {

      val testPort = 8086

      configureFor(testPort)
      val api = new WireMockServer(options.port(testPort))
      val source = new JobResultTarget(s"http://localhost:$testPort/job")

      Given("the source API")
      api.start()

      When("Jobs are pushed to the scheduler API")
      val result = Await.result(source.push(DeliusPushResult(
        Job("5582cb83-ebd4-4e90-8b8d-f65eae81d79a",Map[String,String](),"/thing",Some("body"),"POST"),
        Some(StatusCodes.Accepted),
        None,
        Some("thing"),
        None
      )), 5.seconds)

      Then("the API receives a HTTP POST and returns Accepted")
      verify(
        postRequestedFor(
          urlEqualTo("/job/5582cb83-ebd4-4e90-8b8d-f65eae81d79a"))
      )
      result.status shouldBe Some(StatusCodes.Accepted)

      api.stop()
    }
  }

  override def afterAll() {

    system.terminate()
  }
}
