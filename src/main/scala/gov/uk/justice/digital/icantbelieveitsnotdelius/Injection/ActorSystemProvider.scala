package gov.uk.justice.digital.icantbelieveitsnotdelius.Injection

import akka.actor.ActorSystem
import com.google.inject.Provider

class ActorSystemProvider extends Provider[ActorSystem] {

  override def get() = ActorSystem("pollpush")
}
