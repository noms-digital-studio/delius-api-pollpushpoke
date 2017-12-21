package gov.uk.justice.digital.icantbelieveitsnotdelius

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import gov.uk.justice.digital.icantbelieveitsnotdelius.Injection._
import gov.uk.justice.digital.icantbelieveitsnotdelius.services.{DeliusTarget, JobResultTarget, JobSource}
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.{DeliusSingleTarget, JobResultSingleTarget, SingleSource}
import net.codingwell.scalaguice.ScalaModule
import org.json4s.Formats

import scala.util.Properties

class Configuration extends AbstractModule with ScalaModule {

  private def envOrDefault(key: String) = Properties.envOrElse(key, envDefaults(key))

  private def bindNamedValue[T: Manifest](name: String, value: T) = bind[T].annotatedWithName(name).toInstance(value)

  private def bindConfiguration[T: Manifest](map: Map[String, String], transform: String => T) =

    for ((name, value) <- map.mapValues(envOrDefault).mapValues(transform)) bindNamedValue(name, value)


  protected def envDefaults = Map(
    "DEBUG_LOG" -> "false",
    "JOB_SCHEDULER_BASE_URL" -> "http://localhost:8080/job",
    "DELIUS_API_BASE_URL" -> "http://localhost:8080/delius",
    "DELIUS_API_USERNAME" -> "username",
    "DELIUS_API_PASSWORD" -> "password",
    "POLL_SECONDS" -> "1"
  )

  override final def configure() {

    bindConfiguration(
      Map(
        "sourceUrl" -> "JOB_SCHEDULER_BASE_URL",
        "targetUrl" -> "DELIUS_API_BASE_URL",
        "jobTargetUrl" -> "JOB_SCHEDULER_BASE_URL",
        "username" -> "DELIUS_API_USERNAME",
        "password" -> "DELIUS_API_PASSWORD"
      ),
      identity
    )

    bindConfiguration(
      Map(
        "timeout" -> "POLL_SECONDS"
      ),
      s => s.toInt
    )

    bindConfiguration(
      Map(
        "debugLog" -> "DEBUG_LOG"
      ),
      s => s.toBoolean
    )

    bind[Formats].toProvider[FormatsProvider]
    bind[ActorMaterializer].toProvider[ActorMaterializerProvider]

    bind[ActorSystem].toProvider[ActorSystemProvider].asEagerSingleton()

    configureOverridable()
  }

  protected def configureOverridable() {

    bind[SingleSource].to[JobSource]
    bind[DeliusSingleTarget].to[DeliusTarget]
    bind[JobResultSingleTarget].to[JobResultTarget]
  }
}
