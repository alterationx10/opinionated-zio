package test.opinions

import zio.*
import zio.test.*
import zio.mock.*
import opinions.*

object MocksSpec extends ZIOSpecDefault:

  trait SomeService:
    def get(id: Int): Task[String]
    def multi(a: String, b: Int): Task[String]

  object SomeMockService extends Mock[SomeService]:
    object Get   extends Effect[Int, Throwable, String]
    object Multi extends Effect[(String, Int), Throwable, String]

    val compose: URLayer[Proxy, SomeService] =
      ZLayer {
        for {
          proxy <- ZIO.service[Proxy]
        } yield new SomeService:
          override def get(id: RuntimeFlags): Task[String]             = proxy(Get, id)
          override def multi(a: String, b: RuntimeFlags): Task[String] =
            proxy(Multi, a, b)
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
      },
      test("when => expect") {
        val capturedWhen = SomeMockService.Get.when(42)
        for {
          a  <- ZIO
                  .serviceWithZIO[SomeService](_.get(42))
                  .provide(
                    capturedWhen.expect("a")
                  )
          b  <- ZIO
                  .serviceWithZIO[SomeService](_.get(42))
                  .provide(
                    capturedWhen.expect("b")
                  )
          c  <- ZIO
                  .serviceWithZIO[SomeService](_.get(42))
                  .provide(
                    SomeMockService.Get.when(42.eqTo).expect("c")
                  )
          d  <- ZIO
                  .serviceWithZIO[SomeService](_.get(42))
                  .provide(
                    SomeMockService.Get.when(42).expect("d".expected)
                  )
          e1 <- ZIO
                  .serviceWithZIO[SomeService](_.get(42))
                  .flip
                  .provide(
                    capturedWhen.expectF(new Exception("boom"))
                  )
          e2 <- ZIO
                  .serviceWithZIO[SomeService](_.get(42))
                  .flip
                  .provide(
                    SomeMockService.Get
                      .when(42)
                      .expect(new Exception("boom2").expectedF)
                  )
          _  <- ZIO.unit
        } yield assertTrue(
          a == "a",
          b == "b",
          c == "c",
          d == "d",
          e1.getMessage == "boom",
          e2.getMessage == "boom2"
        )
      },
      test("tuple assertions") {
        val Accessor = ZIO.serviceWithZIO[SomeService]
        // We will use this as a recovery when our expectation fails,
        // so our mock doesn't blow up the test.
        val recovery = SomeMockService.Multi
          .when(Assertion.anything)
          .expectF(new Exception("boom"))
        // We only care about the first arg
        val justA    = SomeMockService.Multi
          .when(("a".eqTo, Assertion.anything).asserted)
          .expect("success") || recovery
        // We only care about the second arg
        val just1    = SomeMockService.Multi
          .when((Assertion.anything, 1.eqTo).asserted)
          .expect("success") || recovery
        // We care about both args
        val a1       = SomeMockService.Multi
          .when(("a".eqTo, 1.eqTo).asserted)
          .expect("success") || recovery
        // We don't care about anything,
        // event just using Assertion.anything instead of this Tuple nonsense :-)
        val anyAny   = SomeMockService.Multi
          .when((Assertion.anything, Assertion.anything).asserted)
          .expect("success") || recovery

        for {
          _ <- Accessor(_.multi("a", 1))
                 .provide(justA)
          _ <- Accessor(_.multi("a", 2))
                 .provide(justA)
          _ <- Accessor(_.multi("b", 1)).flip
                 .provide(justA)
          _ <- Accessor(_.multi("a", 1))
                 .provide(just1)
          _ <- Accessor(_.multi("b", 1))
                 .provide(just1)
          _ <- Accessor(_.multi("a", 2)).flip
                 .provide(just1)
          _ <- Accessor(_.multi("a", 1))
                 .provide(a1)
          _ <- Accessor(_.multi("a", 2)).flip
                 .provide(a1)
          _ <- Accessor(_.multi("b", 1)).flip
                 .provide(a1)
          _ <- Accessor(_.multi("b", 2)).flip
                 .provide(a1)
          _ <- ZIO
                 .foreachDiscard(0 to 99) { _ =>
                   for {
                     strLen <- Random.nextIntBetween(1, 100)
                     str    <- Random.nextString(strLen)
                     int    <- Random.nextIntBetween(-1000, 1000)
                     _      <- Accessor(_.multi(str, int))
                   } yield ()
                 }
                 .provide(anyAny.exactly(100))
        } yield assertCompletes
      }
    )
