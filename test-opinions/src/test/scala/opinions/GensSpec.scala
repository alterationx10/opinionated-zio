package opinions

import zio.*
import zio.test.*
import opinions.*
import zio.test.magnolia.DeriveGen

import java.time.Instant
import java.util.UUID

object GensSpec extends ZIOSpecDefault:

  case class SomeModel(a: Int, b: String, c: Instant, d: UUID)
  given Gen[Any, SomeModel] = DeriveGen[SomeModel]

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("GensSpec")(
      test("Generate a random List of case class instances") {
        for {
          someNumber <- Random.nextIntBetween(10, 50)
          models     <- GenRandom[SomeModel](someNumber)
        } yield assertTrue(
          models.length == someNumber,
          models.distinct.length == someNumber
        )
      },
      test("Generate a random instance of a case class") {
        for {
          model <- GenRandom[SomeModel]
        } yield assertCompletes
      }
    )
