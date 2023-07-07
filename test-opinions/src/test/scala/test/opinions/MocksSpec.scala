package test.opinions

import zio.*
import zio.test.*
import zio.mock.*
import opinions.*

object MocksSpec extends ZIOSpecDefault:

  trait SomeService:
    def get(id: Int): Task[String]

  object SomeMockService extends Mock[SomeService]:
    object Get extends Effect[Int, Throwable, String]

    val compose: URLayer[Proxy, SomeService] =
      ZLayer {
        for {
          proxy <- ZIO.service[Proxy]
        } yield new SomeService:
          override def get(id: RuntimeFlags): Task[String] = proxy(Get, id)
      }
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("MocksSpec + ")(
      test("expectWhen of values") {
        for {
          result <- ZIO
                      .serviceWithZIO[SomeService](_.get(42))
                      .provide(SomeMockService.Get.expectWhen("forty two", 42))
          _      <- ZIO
                      .serviceWithZIO[SomeService](_.get(42))
                      .provide(
                        // Comparison without expectWhen extension method
                        SomeMockService
                          .Get(Assertion.equalTo(42), Expectation.value("forty two"))
                      )
        } yield assertTrue(result == "forty two")
      },
      test("expectWhenF of values") {
        for {
          error <-
            ZIO
              .serviceWithZIO[SomeService](_.get(42))
              .flip
              .provide(
                SomeMockService.Get.expectWhenF(new Exception("boom"), 42)
              )
        } yield assertTrue(error.getMessage == "boom")
      },
      test("expectWhen of assertions") {
        for {
          result <-
            ZIO
              .serviceWithZIO[SomeService](_.get(42))
              .provide(
                SomeMockService.Get.expectWhen("forty two".expected, 42.eqTo)
              )
          error  <-
            ZIO
              .serviceWithZIO[SomeService](_.get(42))
              .flip
              .provide(
                SomeMockService.Get
                  .expectWhen(new Exception("boom").expectedF, 42.eqTo)
              )
        } yield assertTrue(
          result == "forty two",
          error.getMessage == "boom"
        )
      }
    )