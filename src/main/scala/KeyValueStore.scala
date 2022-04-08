import akka.Done
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object KeyValueStore {

  sealed trait KeyValueOperation[K,V]
  case class Get[K,V](key: K, replyTo: ActorRef[Option[V]] ) extends KeyValueOperation[K,V]
  case class Put[K,V](key: K, value: V, replyTo: ActorRef[Done]) extends KeyValueOperation[K,V]
  case class HasKey[K,V](key: K, replyTo: ActorRef[Boolean]) extends KeyValueOperation[K,V]
  case class Count(replyTo: ActorRef[Int]) extends KeyValueOperation[Nothing, Nothing]
  case class Remove[K](key: K, replyTo: ActorRef[Done]) extends KeyValueOperation[K, Nothing]

  def apply[K,V](): Behaviors.Receive[KeyValueOperation[K, V]] = initialize(Map.empty[K,V])

  private def initialize[K,V](state: Map[K,V]): Behaviors.Receive[KeyValueOperation[K, V]] = Behaviors.receiveMessage[KeyValueOperation[K,V]]{ operation =>
      (operation: KeyValueOperation[K,V], state: Map[K,V]) match {

        case (Get(key, replyTo), state: Map[K,V]) =>
          replyTo ! state.get(key)
          Behaviors.same

        case (Put(k,v,replyTo), state: Map[K,V]) =>
          replyTo ! Done
          initialize(state + (k -> v))

        case (HasKey(key, replyTo), state) =>
          replyTo ! state.contains(key)
          Behaviors.same
      }
    }
}