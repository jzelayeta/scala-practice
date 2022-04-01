import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MoneyCaseClassSpec extends AnyWordSpecLike with Matchers {

  case class Money(currency: String, value: BigDecimal) {
    val int = 2;
    var double = 3;

    def multiply(factor: Double) = this.copy(value = this.value * factor)

    def add(otherMoney: Money) = {
      require(this.currency == otherMoney.currency, "currencies must be the same")
      this.copy(value = value + otherMoney.value)
    }
  }

  object Money {
    val ARS_TO_USD = 205
    def ARS_MONEY(value: BigDecimal) = Money("ARS", value)
    def apply(value: BigDecimal): Money = Money("ARS", value)
  }


  "Money" should {

    val money = Money("ARS", 100)
    Money.ARS_MONEY(100)
    Money.ARS_TO_USD

    "have currency" in {
      money.currency shouldBe "ARS"
    }
    "have a value" in {
      money.value shouldBe 100
    }

    "multiply by a factor" in {
      money.multiply(2) shouldBe Money("ARS", 200)
    }

    "add to other money" in {
      val otherMoney = Money("ARS", 200)
      (otherMoney add money) shouldBe Money("ARS", 300)
    }

    "add two money objects with different currencies" in {
      val otherMoney = Money("USD", 200)
      assertThrows[IllegalArgumentException]{
        (otherMoney add money)
      }
    }
    "asdasd" in {
      val m = Money(100)
      m.double = 6
      m.double shouldBe 6
    }
  }

  "Pattern matching" should {

    case class Person(name: String,
                      surname: String,
                      age: Int,
                      dni: String,
                      address: String,
                      telephone: String)
    "asda" in {

      val l = List(1,2,3,4,5)

      l match {
        case Nil => println("esta vacia")
        case elem :: Nil  => println(s"tiene un solo elemento ${elem}")
        case elem :: tail => println(s"tiene un solo elemento ${elem} con una cola ${tail}")
        case 1 :: _       => println("la lista arranca con 1")
      }

      1 shouldBe 1

      val nick = "nick"
      val zeta = "nick"

      (nick, zeta) match {
        case (`zeta`, `nick`)  => println("son iguales")
        case _                 => println("son distintos")
      }
    }
  }
}
