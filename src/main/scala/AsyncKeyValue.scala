import scala.concurrent.{ExecutionContext, Future}

trait AsyncKeyValue[K,V] {
  def put(key: K, value: V)(implicit ec: ExecutionContext): Future[Map[K,V]]
  def remove(key: K)(implicit ec: ExecutionContext): Future[Unit]
  def get(key: K)(implicit ec: ExecutionContext): Future[Option[(K,V)]]
  def get(keys: K*)(implicit ec: ExecutionContext): Future[Map[K,V]]
}
