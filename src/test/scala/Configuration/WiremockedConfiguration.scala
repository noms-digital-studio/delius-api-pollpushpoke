package Configuration

import gov.uk.justice.digital.icantbelieveitsnotdelius.Configuration

class WiremockedConfiguration(deliusUrl: String, schedulerUrl: String) extends Configuration {

  override def envDefaults = super.envDefaults + (
    "JOB_SCHEDULER_BASE_URL" -> schedulerUrl,
    "DELIUS_API_BASE_URL" -> deliusUrl)
}
