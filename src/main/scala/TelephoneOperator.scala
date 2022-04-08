import akka.actor.{Actor, ActorLogging, Stash}

import java.time.{Duration, Instant}
import java.util.UUID

sealed trait CallAction {
  val callId: UUID
}

case class StartCall(callId: UUID= UUID.randomUUID()) extends CallAction
case class EndCall(callId: UUID) extends CallAction

sealed trait CallEvent {
  val callId: UUID
}

case class CallStarted(callId: UUID, started: Instant = Instant.now()) extends CallEvent
case class CallQueued(callId: UUID, dateTime: Instant = Instant.now()) extends CallEvent
case class CallEnded(callId: UUID, duration: Duration) extends CallEvent
case class CallNotFound(callId: UUID) extends CallEvent


class TelephoneOperator extends Actor with ActorLogging with Stash {

  override def receive: Receive = ???
}
