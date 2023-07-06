package opinions

import zio.*
import zio.test.*
import opinions.*

case class TestConfig(a: String, b: Int)
trait TestService
case class TestServiceImpl(port: Int, config: TestConfig) extends TestService
    derives AutoLayer

object ZIOSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ZIOSpec")(
      test("converts a value to a UIO")(
        for {
          result <- 42.uio
        } yield assertTrue(result == 42)
      ),
      test("converts a value to a ULayer")(
        for {
          result <- ZIO.service[Int].provide(42.ulayer)
        } yield assertTrue(result == 42)
      ),
      test("converts a zio to a zlayer") {
        for {
          result      <- ZIO.service[Int].provide(ZIO.attempt(42).zlayer)
          otherResult <- ZIO.service[String].provide("test".uio.zlayer)
        } yield assertTrue(
          result == 42,
          otherResult == "test"
        )
      },
      test("Load a config model via layer") {
        for {
          config <-
            ZIO
              .service[TestConfig]
              .provide(ConfigLayer[TestConfig]("com.alterationx10.testconfig"))
          error  <-
            ZIO
              .service[TestConfig]
              .provide(
                ConfigLayer[TestConfig]("com.alterationx10.does-not-exist")
              )
              .flip
        } yield assertTrue(
          config.a == "hello",
          config.b == 1234
        )
      },
      test("Auto derives a layer for a case class") {
        for {
          service <- ZIO
                       .service[TestServiceImpl]
                       .provide(
                         AutoLayer[TestServiceImpl],
                         0.ulayer,
                         ConfigLayer[TestConfig]("com.alterationx10.testconfig")
                       )
        } yield assertTrue(
          service.port == 0,
          service.config.a == "hello",
          service.config.b == 1234
        )
      },
      test("Auto derives a layer for a service as the parent type") {
        for {
          service <- ZIO
                       .service[TestService]
                       .provide(
                         AutoLayer.as[TestService, TestServiceImpl],
                         0.ulayer,
                         ConfigLayer[TestConfig]("com.alterationx10.testconfig")
                       )
        } yield assertCompletes
      }
    )
