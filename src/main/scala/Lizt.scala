
sealed trait Lizt[+A] { self: Lizt[A] => //self types
  def ::[B >: A](elem: B): Lizt[B] = Conz(elem, self)

  def :+[B >: A](elem: B): Lizt[B] = self match {
    case Conz(head, Nilz) => Conz(head, Conz(elem, Nilz))
    case Conz(head, tail) => Conz(head, tail :+ elem)
    case Nilz => Conz(elem, Nilz)
  }

  def headOption(): Option[A] = self match {
    case Conz(head, _) => Some(head)
    case Nilz => None
  }

  def size(): Int = self match {
    case Conz(_, tail) => 1 + tail.size()
    case Nilz          => 0
  }

  def reverse(): Lizt[A] = self match {
    case x@Conz(head, Nilz) => x
    case Conz(head, tail) => tail.reverse() :+ head
    case Nilz => Nilz
  }

  def fold[B](seed: B)(f: (B, A) => B): B = self match {
    case Conz(head, Nilz) => f(seed, head)
    case Conz(head, tail) => f(tail.fold(seed)(f), head)
    case Nilz => seed
  }
}

case class Conz[A](head: A, tail: Lizt[A]) extends Lizt[A]

case object Nilz extends Lizt[Nothing]

object Lizt {

   def apply[A](elems: A*): Lizt[A] = elems.toList match {
    case head :: Nil  => Conz(head, Nilz)
    case head :: tail => Conz(head, Lizt.apply(tail:_*))
    case Nil => Nilz
  }


}
