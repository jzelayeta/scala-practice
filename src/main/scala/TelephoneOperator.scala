import akka.actor.{Actor, ActorLogging, Stash}

import java.time.{Duration, Instant}
import java.util.UUID

sealed trait CallAction {
  val callId: UUID
}

case class StartCall(callId: UUID = UUID.randomUUID()) extends CallAction
case class EndCall(callId: UUID) extends CallAction

sealed trait CallEvent {
  val callId: UUID
}

case class CallStarted(callId: UUID, started: Instant = Instant.now()) extends CallEvent
case class CallQueued(callId: UUID, dateTime: Instant = Instant.now()) extends CallEvent
case class CallEnded(callId: UUID, duration: Duration) extends CallEvent
case class CallNotFound(callId: UUID) extends CallEvent

class TelephoneOperator extends Actor with ActorLogging with Stash {

  var currentCall: Option[CallStarted] = Option.empty

  override def receive: Receive = {
    case StartCall(id) =>
      val replyTo = sender()
      val callStarted = CallStarted(id)
      currentCall = Some(callStarted)
      replyTo ! callStarted
      stash()
      context.become(occupied)
    case EndCall(id)  =>
      sender() ! CallNotFound(id)
    case otherEvent  =>
      sender() ! new Exception(s"$otherEvent is unhandled")

  }

  def occupied: Receive = {
    case StartCall(id) =>
      println(s"estado ocupado, me llego un StartCall $id")
      sender() ! CallQueued(id)
    case EndCall(idToEnd) =>
      val replyTo = sender()
      val endTime = Instant.now()
      currentCall match {
        case Some(cs@CallStarted(`idToEnd`, _)) =>
          val callDuration = Duration.between(cs.started, endTime)
          currentCall = None
          replyTo ! CallEnded(idToEnd, callDuration)
          context.unbecome()
        case _ => ???
      }
  }

}
