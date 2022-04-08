import akka.Done
import akka.actor.{Actor, ActorLogging, Stash}
import akka.actor.typed.ActorRef


sealed trait KeyValueOperation[K,V]
case class Get[K,V](key: K) extends KeyValueOperation[K,V]
case class Put[K,V](key: K, value: V) extends KeyValueOperation[K,V]
case class HasKey[K,V](key: K) extends KeyValueOperation[K,V]
case object Count extends KeyValueOperation[Nothing, Nothing]
case class Remove[K](key: K) extends KeyValueOperation[K, Nothing]

class KeyValueStoreClassic[K,V] extends Actor with ActorLogging with Stash {

  private var state: Map[K,V] = Map.empty


  //! tell => no espera respuesta
  //? ask => espera respuesta

  override def receive: Receive = {
    case get: Get[K,V] =>
      val maybeTuple: Option[(K, V)] = state.get(get.key).map(value => get.key -> value)
      sender() ! maybeTuple

    case put: Put[K,V] =>
      log.debug(s"inserted ${put.key} -> ${put.value}")
      state = state + (put.key -> put.value)
      sender() ! Done

    case _ => stash()

  }

}
