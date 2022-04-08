import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpecLike

class LiztSpec extends AnyWordSpecLike with Matchers {

 "Lizt" should {
   "head" in {

     val l: Lizt[Int] = 1 :: 2 :: 3 :: Nilz

     l.headOption() shouldBe Some(1)

     Nilz.headOption() shouldBe None

   }

   "add element to list" in {
     val lizt = (1 :: 2 :: 3 :: Nilz) :+ 4
     lizt.size() shouldBe 4
   }

   "size" in {
     Nilz.size() shouldBe 0

     (1 :: 2 :: Nilz).size() shouldBe 2
   }

   "reverse" in {
     Nilz.reverse() shouldBe Nilz

     Lizt(1,2,3).reverse() shouldBe Lizt(3,2,1)

     def myFunction(int: Int)(f: Int => String): String = f(int)

     val f: (Int => String) => String = myFunction(2)

     val g: String => Option[String] = myString => Some(myString)

     val h: (Int => String) => Option[String] = (f andThen g)
     h.apply(int => int.toString)


   }

 }

}
