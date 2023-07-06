package opinions

import zio.*
import zio.test.*
import opinions.*

import scala.util.Try

object TrysSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("TrysSpec")(
      test("Safely converts a Try to an Option") {
        assertTrue(
          Try(42).safeOpt.contains(42),
          Try(throw new Exception("Boom")).safeOpt.isEmpty,
          Try(null).safeOpt.isEmpty
        )
      }
    )
