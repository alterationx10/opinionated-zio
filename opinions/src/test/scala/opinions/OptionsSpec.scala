package opinions

import zio.*
import zio.test.*
import opinions.*

object OptionsSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("OptionsSpec")(
      test("wraps a value in Option.apply") {
        assertTrue(
          1.opt.contains(1),
          "string".opt.contains("string"),
          null.opt.isEmpty
        )
      },
      test("wraps a value in Some") {
        assertTrue(
          1.some.contains(1),
          "string".some.contains("string"),
          null.some.contains(null)
        )
      }
    )
