import akka.actor.{Actor, ActorLogging, Stash}

import java.time.{Duration, Instant, LocalDateTime}
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

  var currentCall: Option[CallStarted] = Option.empty
  var pendingCalls: List[CallQueued] = List.empty

  def occupied: Receive = {
    case StartCall(id) =>
      val queued = CallQueued(id)
      pendingCalls = pendingCalls :+ queued
      sender() ! queued
      stash()
    case EndCall(callId) =>
     endCall(callId)
  }

  override def receive: Receive = {
    case StartCall(id) =>
      val callStarted = CallStarted(id)
      sender() ! callStarted
      currentCall = Some(callStarted)
      context.become(occupied)

    case EndCall(callId) =>
     endCall(callId)
  }

  private def endCall = (callIdToEnd: UUID) => {
    println(s"receive end call $callIdToEnd, while current call is ${currentCall}")
    currentCall match {
      case Some(call@CallStarted(`callIdToEnd`, _)) =>
        val endTime = Instant.now()
        pendingCalls = pendingCalls.filter(_.callId != callIdToEnd)
        currentCall = None
        sender() ! CallEnded(callIdToEnd, Duration.between(call.started, endTime))
        context.unbecome()
        unstashAll()
      case Some(_) if pendingCalls.map(_.callId).contains(callIdToEnd) =>
        println(s"Stashing end call for ${callIdToEnd}")
        stash()
      case _  =>
        sender() ! CallNotFound(callIdToEnd)
    }
  }


}
