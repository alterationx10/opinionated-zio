# Opinionated ZIO

A collection of opinionated extensions and helpers around ZIO and vanilla
Scala 3 (and only Scala 3).

![Maven Central](https://img.shields.io/maven-central/v/com.alterationx10/opinionated-zio_3)

## Installation

Check the badge above, or the latest GitHub release for the latest version
to replace `x.y.z`.

### sbt

```scala
libraryDependencies += "com.alterationx10" %% "opinionated-zio" % "x.y.z"
libraryDependencies += "com.alterationx10" %% "opinionated-zio-test" % "x.y.z" % Test
```

### scala cli

```scala
//> using dep com.alterationx10::opinionated-zio:x.y.z
//> using test.dep com.alterationx10::opinionated-zio-test:x.y.z
```

### mill

```scala
ivy"com.alterationx10::opinionated-zio:x.y.z"
ivy"com.alterationx10::opinionated-zio-test:x.y.z"
```

## Example Usages

Everything is bundled into one package, and to use it, you only need to

```scala
import opinons.*
```

Below are some example usages, centered around the file they're located in.
You can also see usage examples in the test suite.

### Options

```scala

// Wrap a pure value in Option.apply
val anOpt: Option[String] = "example".opt

// Wrap a pure value in Some
val someOpt: Option[String] = "example".some

```

### Trys

```scala
// Wraps the result of a Try in Option.apply, converts the Try to 
// an option, and then flattens
val safeTryOpt: Option[String] = Try(null).safeOpt // returns None
```

### ZIO

```scala

// Wraps a value in a ZLayer.succeed
val stringLayer: ULayer[String] = "layer".ulayer

// Wraps a value in ZIO.succeed
val stringZIO: UIO[String] = "effect".uio

// Wraps an effect in ZLayer
val effectLayer: ZLayer[Any, Throwable, String] =
  ZIO.attempt("layer").zlayer

// Map a case class to a typelevel config path
case class Config(a: String, b: Int)

val configLayer: Layer[ReadError[String], Config] =
  ConfigLayer[Config]("some.config.path")

// Automatically derive a generic ZLayer
trait Service

// My markdown formatter keeps moving 'derives AutoLayer', so I added it 
// behind a comment line here :-(
case class ServiceImpl(p: Int, cfg: Config) extends Service //derives AutoLayer

val implLayer: ZLayer[Int & Config, Nothing, ServiceImpl] =
  AutoLayer[ServiceImpl]

val superLayer: ZLayer[Int & Config, Nothing, Service] =
  AutoLayer.as[Service, ServiceImpl]


```

## Example Test Library Usages

Everything is bundled into one package for the test library as well, and to use
it, you only need to

```scala
import test.opinons.*
```

More docs to come, but please check the corresponding tests for examples.

### Assertions

```scala

// Wrap a value in Assertion.equalTo
val eq1: Assertion[Int] = 1.eqTo

// Negates wrapping a value in Assertion.EqualTo
val neq1: Assertion[Int] = 1.neqTo

```

### Expectations

```scala

// Wrap a value in Expectation.value
val expect1: Result[Any, Nothing, Int] = 1.expected

// Wrap a value in Expectation.failure
val fail: Result[Any, Throwable, Nothing] = new Exception("boom").expectedF


```

### Gens

ZIO has Gens for generative testing, but I also like to use them to make
instances of models in other tests (i.e. integration tests for DB, etc...).

```scala

case class Thing(a: String)

// Am implicit Gen[Any, Thing] will need to be in scope.
// You can use something like this to derive a Gen if needed
// given Gen[Any, Thing] = DeriveGen[Thing]

// Generate a List of 42 random Things
val thingList: UIO[List[Thing]] = GenRandom[Thing](42)

// Generate a random Thing
val thing: UIO[Thing] = GenRandom[Thing]

```

### Mocks

These methods are mostly to add different ergonomics around Expectations as
layers. It adds things `expectWhen` (or `whenExpect`), `expectWhenF` (for
failures), and `.when(???).expect(???)` (partially applying the arguments).

Considering the following Service + Mock

```
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
```

We could do some the following:

```scala
      test("expect/when of values") {
  for {
    result <- ZIO
      .serviceWithZIO[SomeService](_.get(42))
      .provide(SomeMockService.Get.expectWhen("forty two", 42))
    // partial application extensions
    _ <- ZIO
      .serviceWithZIO[SomeService](_.get(42))
      .provide(
        SomeMockService.Get
          .when(42)
          .expect("c")
      )
    // Comparison without extension method
    _ <- ZIO
      .serviceWithZIO[SomeService](_.get(42))
      .provide(
        SomeMockService
          .Get(Assertion.equalTo(42), Expectation.value("forty two"))
      )
    // Mocking a failure
    error <-
      ZIO
        .serviceWithZIO[SomeService](_.get(42))
        .flip
        .provide(
          SomeMockService.Get.expectWhenF(new Exception("boom"), 42)
        )
  } yield assertTrue(
    result == "forty two",
    error.getMessage == "boom"
  )
}
```

One pain with mocking services that multi-argument are Tuples, but expects
a (single) Assertion of that Tuple type.

It would be wonderful to provide assertions on the tuple elements, instead of
one blanket assertion of a properly typed tuple. For example, maybe I only care
about the value of the first element of a Tuple3, and the others can be
anything.

This provides (up to Tuple5 so far, because I'm lazy) extension methods to
take a Tuple of Assertions to make it an Assertion of the appropriately
typed Tuple, checking the values along the way.

These are generally implemented as:

```scala
extension[A, B, C](t: (Assertion[A], Assertion[B], Assertion[C]))
/** Convert a Tuple of Assertions into an Assertion of a Tuple
 */
def asserted: Assertion[(A, B, C)] =
  Assertion.hasField("_1", (tt: (A, B, C)) => tt._1, t._1) &&
    Assertion.hasField("_2", (tt: (A, B, C)) => tt._2, t._2) &&
    Assertion.hasField("_3", (tt: (A, B, C)) => tt._3, t._3)
```

With these, we can write a Mock expectation like below, where we only care
about a specific value for the first method argument, but the second can be
anything.

```scala
val justA = SomeMockService.Multi
  .when(("a".eqTo, Assertion.anything).asserted)
  .expect("success")
```