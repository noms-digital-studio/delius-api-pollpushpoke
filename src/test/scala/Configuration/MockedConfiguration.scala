package Configuration

import gov.uk.justice.digital.icantbelieveitsnotdelius.Configuration
import gov.uk.justice.digital.icantbelieveitsnotdelius.traits.{DeliusSingleTarget, JobResultSingleTarget, SingleSource}

class MockedConfiguration(deliusSingleTarget: DeliusSingleTarget, jobResultSingleTarget: JobResultSingleTarget, singleSource: SingleSource) extends Configuration {

  override def envDefaults = super.envDefaults +
    ("JOB_SCHEDULER_BASE_URL" -> "http://localhost:8080/job",
      "DELIUS_API_BASE_URL" -> "http://localhost:8080/delius")

  override protected def configureOverridable() {
    bind[DeliusSingleTarget].toInstance(deliusSingleTarget)
    bind[JobResultSingleTarget].toInstance(jobResultSingleTarget)
    bind[SingleSource].toInstance(singleSource)
  }
}
