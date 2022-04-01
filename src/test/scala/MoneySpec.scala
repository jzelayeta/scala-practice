import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike


class Money(var currency: String , var value: BigDecimal) {

  def multiply(factor: Double) = new Money(this.currency, value * factor)

  def add(otherMoney: Money) = {
    require(this.currency == otherMoney.currency, "currencies must be the same")
    new Money(currency, this.value + otherMoney.value)
  }
}

class MoneySpec extends AnyWordSpecLike with Matchers {

  val money = new Money("ARS", 100)

  "Money" should {
    "have currency" in {
      money.currency shouldBe "ARS"
    }
    "have a value" in {
      money.value shouldBe 100
    }

    "multiply by a factor" in {
      val newMoney = money.multiply(2)
      newMoney.value shouldBe 200
    }

    "add to other money" in {
      val otherMoney = new Money("ARS", 50)
      val newMoney = money.add(otherMoney)
      newMoney.value shouldBe 250
    }
  }

}
