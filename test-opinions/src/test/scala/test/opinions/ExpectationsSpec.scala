package test.opinions

import zio.*
import zio.test.*
import zio.mock.*

object ExpectationsSpec extends ZIOSpecDefault:

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ExpectationsSpec")(
      test("expected") {
        assertZIO(42.expected.io(()))(42.eqTo)
      },
      test("expectedF") {
        assertZIO(42.expectedF.io(()).flip)(42.eqTo)
      }
    )
