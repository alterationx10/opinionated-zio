package test.opinions

import zio.*
import zio.test.*
import opinions.*

object AssertionsSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("AssertionsSpec")(
      test("eqTo") {
        assertZIO(42.uio)(42.eqTo)
      },
      test("neqTo") {
        assertZIO(42.uio)(69.neqTo)
      }
    )
