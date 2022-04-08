import akka.{Done, actor}
import akka.actor.Props
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Scheduler}
import akka.dispatch.Futures
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.Random

class KeyValueStoreSpec extends AsyncWordSpecLike with Matchers {

  import akka.actor.typed.scaladsl.AskPattern._

  val system: ActorSystem[KeyValueStore.KeyValueOperation[String, String]] =
  ActorSystem(KeyValueStore[String,String](), "kv")

  implicit val timeout: Timeout = Timeout(3.seconds)
  private implicit val scheduler: Scheduler = system.scheduler

  "Class custom" should {

    val pool = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))

    "get from empty map" in {
      val kv: AsyncKeyValue[String, String] = new AsyncKeyValueStore[String, String]()

      val eventualMaybeTuple: Future[Option[(String, String)]] = kv.get("zeta")(pool)

      eventualMaybeTuple
        .map(tuple => tuple shouldBe None)

      for {
        result <- eventualMaybeTuple
      } yield {
        result shouldBe None
      }
    }

    "put & get" in {

      val kv: AsyncKeyValue[Int, Int] = new AsyncKeyValueStore[Int, Int]()

      val peopleRoles = (1 to 10000).toList

      val future = Future.traverse(peopleRoles){i => kv.put(i,i)(pool)}


      for {
        _   <- future
        get <- kv.get(9999)
      } yield {
        get shouldBe Some(9999 -> 9999)
      }
    }

    //THIS IS A FLAKY TEST
    "put concurrency" in {
      val kv: AsyncKeyValue[String, String] = new AsyncKeyValueStore[String, String]()

      //Non thread-safe
      val keysAndValues: Seq[(String, String)] = (1 to 1000).map { i =>
        if (i % 2 == 0) "zeta" -> "tl" else "zeta" -> "dev"
      }

      for {
        _     <- Future.traverse(keysAndValues){case (k,v) => kv.put(k,v)(pool)}
        value <- kv.get("zeta")(pool)
      } yield {
        value.get shouldBe "zeta" -> "tl"
      }

    }

    "multi get" in {
      val kv: AsyncKeyValue[String, String] = new AsyncKeyValueStore[String, String]()

      val peopleTeams = List("zeta" -> "catalog", "fran" -> "payments", "nick" -> "catalog", "mati" -> "payments")
      val future = Future.traverse(peopleTeams){case (person, team) => kv.put(person, team)(pool)}

      for {
        _       <- future
        request = List("zeta", "fran", "nick", "pepe", "pipi")
        result  <- kv.get(request:_*)(pool)
      } yield {
        val list: List[(String, Option[String])] = ???
        list should contain theSameElementsAs List("zeta" -> Some("catalog"), "fran" -> Some("payments"), "nick" -> Some("catalog"), "pepe" -> None, "pipi" -> None)
      }
    }
  }

  "Classic" should {
    val classicSystem = actor.ActorSystem("test-system")
    val props = Props[KeyValueStoreClassic[String, String]]()
    val actorRef: actor.ActorRef = classicSystem.actorOf(props) //aca cree mi actor

    "get none" in {
      val future: Future[Option[String]] = (actorRef ? Get("zeta")).mapTo[Option[String]]

      for {
        result <- future
      } yield {
        result shouldBe None
      }
    }

    "put" in {
      actorRef ! Put("zeta", "tl")

      for {
        result <- (actorRef ? Get("zeta")).mapTo[Option[(String, String)]]
      } yield {
        result shouldBe Some("zeta" -> "tl")
      }
    }

    "put concurrent" in {
      val keysAndValues = (1 to 1000).map { i =>
        if (i % 2 == 0) actorRef ! Put("zeta", "tl") else actorRef ! Put("zeta", "dev")
      }

      Thread.sleep(2000)

      for {
        result <- (actorRef ? Get("zeta")).mapTo[Option[String]]
      } yield {
        result shouldBe Some("tl")
      }
    }
  }

  "Typed" should {
    "get from empty state" in {
      val eventualResponse: Future[Option[String]] = system.ask(KeyValueStore.Get("zeta", _))

      for {
        response: Option[String] <- eventualResponse
      } yield {
        response shouldBe None
      }
    }

    "put" in {
      val eventualPutResponse: Future[Done] = system.ask(KeyValueStore.Put("zeta", "tl", _))
      val eventualGetResponse: Future[Option[String]] = system.ask(KeyValueStore.Get("zeta", _))
      val eventualHasKeyResponse: Future[Boolean] = system.ask(KeyValueStore.HasKey("zeta", _))


      for {
        _ <- eventualPutResponse
        v <- eventualGetResponse
        b <- eventualHasKeyResponse
      } yield {
        v shouldBe Some("tl")
        b shouldBe true
      }
    }


    "multi get" in {
      val keys = List("zeta", "mati", "nick", "seba")


      val futures: List[Future[Option[String]]] = keys.map(key => system.ask(KeyValueStore.Get(key, _)))
      val eventualIterable = Future.sequence(futures)
      eventualIterable

      Future.successful(1 shouldBe 1)
    }

  }


}
