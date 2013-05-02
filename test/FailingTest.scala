import org.specs2.mutable._


class FailingTest extends Specification {

  "This test" should {
    "fail" in {
      "barfoo" must beEqualTo("foobar")
    }
  }

}
