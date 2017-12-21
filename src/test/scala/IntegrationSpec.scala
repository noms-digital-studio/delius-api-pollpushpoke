import Configuration.WiremockedConfiguration
import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import gov.uk.justice.digital.icantbelieveitsnotdelius.Server
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSpec, GivenWhenThen, Matchers}

class IntegrationSpec extends FunSpec with BeforeAndAfter with GivenWhenThen with Eventually with Matchers {

  describe("Full Integration tests") {

    it("pulls a job from source, pushes to target in parallel, and pushes delius result back to the job scheduler") {

      Given("the source system has a job")

      When("the job is received from source in around 2 seconds")
      runServer()

      Then("the job results are pushed to target")
      eventually(tenSecondTimeout) { // Allow 2 seconds to pull and max of 3 seconds to push simultaneously, plus start up time

        verify(getRequestedFor(urlPathEqualTo("/job")))
        verify(postRequestedFor(urlEqualTo("/delius/hello")))
        verify(postRequestedFor(urlEqualTo("/job/5582cb83-ebd4-4e90-8b8d-f65eae81d79a")))
      }
    }
  }
  private val tenSecondTimeout = Timeout(Span(10, Seconds))

  private var mockedRestAPIs: Option[WireMockServer] = None
  private var runningService: Option[ActorSystem] = None

  private val port = 8091

  before {
    mockedRestAPIs = Some(new WireMockServer(port))
    configureFor(port)
    mockedRestAPIs.get.start()
  }

  after {
    mockedRestAPIs.get.stop()
    runningService.get.terminate()
  }

  private def runServer() = runningService = Some(Server.run(new WiremockedConfiguration(s"http://localhost:$port/delius", s"http://localhost:$port/job")))
}
