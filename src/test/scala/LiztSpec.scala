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
   }

   "filter integers" in {
     val lizt = Lizt(1,2,3,4,5,6,7)

     lizt.filter(elem => elem > 2) shouldBe Lizt(3,4,5,6,7)
   }

   "map elements" in {
     val lizt = Lizt(1,2,3,4,5,6,7)

     lizt.map(elem => elem * 2) shouldBe Lizt(2,4,6,8,10,12,14)
   }

   "fold" in {
     val lizt = Lizt(1,2,3,4,5,6,7)

     lizt.fold(0){(e1,e2) => e1 + e2} shouldBe 28
   }

   "flatMap" in {
     val lizt = Lizt(Lizt(1,2,3), Lizt(4,5), Nilz)

     lizt.flatMap(list => list.reverse()) shouldBe Lizt(3,2,1,5,4)
   }



 }

}
