import akka.actor
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import java.time.temporal.TemporalUnit
import java.util.UUID
import scala.List
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.Random

class TelephoneOperatorSpec extends AsyncWordSpecLike with Matchers {


  val classicSystem = actor.ActorSystem("test-system")
  implicit val timeout: Timeout = Timeout(3.seconds)


  val props = Props[TelephoneOperator]()
  val operator: actor.ActorRef = classicSystem.actorOf(props) //aca cree mi actor

  "Operator" should {
    "start new call" in {
      val call = StartCall()
      val future = (operator ? call).mapTo[CallStarted]
      for {
        response <- future
      } yield {
        operator ! EndCall(call.callId)
        response.callId shouldBe call.callId
      }
    }

    "queue call if already occupied" in {
      val call1 = StartCall()
      val call2 = StartCall()

      for {
        response1 <- (operator ? call1).mapTo[CallStarted]
        response2 <- (operator ? call2)
      } yield {
        operator ! EndCall(call1.callId)
        operator ! EndCall(call2.callId)
        response1.callId shouldBe call1.callId
        response2 shouldBe a[CallQueued]
      }
    }

    "end a call that is already attended" in {
      val callId = UUID.randomUUID()
      operator ! StartCall(callId)
      Thread.sleep(1100) //let time pass by 1.1 second...

      for {
        response <- (operator ? EndCall(callId)).mapTo[CallEnded]
      } yield {
        response.callId shouldBe callId
        response.duration should be >= java.time.Duration.ofSeconds(1)
      }
    }

    "end a call already ended" in {
      val callId = UUID.randomUUID()
      operator ! StartCall(callId)

      for {
        response1 <- (operator ? EndCall(callId)).mapTo[CallEvent]
        response2 <- (operator ? EndCall(callId)).mapTo[CallEvent]
      } yield {
        response1 shouldBe a[CallEnded]
        response2 shouldBe a[CallNotFound]
      }
    }

    "end call that does not exist" in {
      for {
        response <- (operator ? EndCall(UUID.randomUUID()))
      } yield {
        response shouldBe a[CallNotFound]
      }
    }

    "start multiple calls and end them in order" in {
      val startedCalls = Future.traverse((1 to 10).toList) { _ => (operator ? StartCall()).mapTo[CallEvent]}

      val startedCallIds: Future[List[UUID]] = startedCalls.map(calls => calls.map(_.callId))

      for {
        idsToEnd <- startedCallIds
        ended <- Future.traverse(idsToEnd) { idToEnd => {
          Thread.sleep(100) //simulates operator on another call....
          (operator ? EndCall(idToEnd)).mapTo[CallEvent]
          }
        }
      } yield {
        idsToEnd should contain theSameElementsAs ended.map(_.callId)
      }
    }

    "start multiple calls and end them randomly" in {
      val startedCalls = Future.traverse((1 to 10).toList) { _ => (operator ? StartCall()).mapTo[CallEvent]}

      val startedCallIds: Future[List[UUID]] = startedCalls.map(calls => Random.shuffle(calls).map(_.callId))

      for {
        idsToEnd <- startedCallIds
        ended <- Future.traverse(idsToEnd) { idToEnd => {
          Thread.sleep(100) //simulates operator on another call....
          (operator ? EndCall(idToEnd)).mapTo[CallEvent]
        }
        }
      } yield {
        idsToEnd should contain theSameElementsAs ended.map(_.callId)
      }
    }

    "properly manage queued calls" in {
      val startedCalls = Future.traverse((1 to 10).toList) { _ => (operator ? StartCall()).mapTo[CallEvent]} //start callls

      val startedCallIds: Future[List[UUID]] = startedCalls.map(calls => Random.shuffle(calls).map(_.callId))

      for {
        idsToEnd     <- startedCallIds
        ended        <- Future.traverse(idsToEnd) { idToEnd => (operator ? EndCall(idToEnd)).mapTo[CallEvent] }.andThen(_ => println("finished first end")) //end calls
        endThemAgain <- Future.traverse(idsToEnd) { idToEnd => (operator ? EndCall(idToEnd)).mapTo[CallNotFound] } //end calls again
      } yield {
        ended shouldBe a[List[CallEnded]]
        idsToEnd should contain theSameElementsAs ended.map(_.callId)

        endThemAgain shouldBe a[List[CallNotFound]]
      }
    }
  }
}
