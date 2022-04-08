import java.util.concurrent.locks.{ReadWriteLock, ReentrantLock, ReentrantReadWriteLock}
import java.util.concurrent.{BlockingQueue, Callable, ExecutorService, Executors}
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future, Promise, blocking}
import scala.util.{Random, Success}

class AsyncKeyValueStore[K,V] extends AsyncKeyValue[K,V]{
  private var state: Map[K,V] = Map.empty

  def put(key:K, value: V)(implicit ec: ExecutionContext): Future[Map[K, V]] = {
    val putPromise = Promise[Map[K,V]]()
    ec.execute{
      () => {
          println(s"inserted $key -> $value")
          state = state + (key -> value)
          putPromise.success(state)
        }
    }
    putPromise.future
  }

  def get(key: K)(implicit ec: ExecutionContext): Future[Option[(K,V)]] = {
    val getPromise = Promise[Option[(K,V)]]()
    ec.execute{
      () => {
          println(s"get $key")
          val result = state.get(key).map(key -> _)
          getPromise.success(result)
      }
    }
    getPromise.future
  }

  override def remove(key: K)(implicit ec: ExecutionContext): Future[Unit] = {
    val putPromise = Promise[Unit]()
    ec.execute{
      () => {
          println(s"removing $key")
          state = state.removed(key)
          putPromise.success()
      }
    }
    putPromise.future
  }

  override def get(keys: K*)(implicit ec: ExecutionContext): Future[Map[K, V]] = ???
}
