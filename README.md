# Opinionated ZIO

A collection of opinionated extensions and helpers around ZIO and vanilla
Scala 3 (and only Scala 3).

![Maven Central](https://img.shields.io/maven-central/v/com.alterationx10/opinionated-zio_3)

## Installation

Check the badge above, or the latest GitHub release for the latest version.

### sbt

```scala
libraryDependencies += "com.alterationx10" %% "opinionated-zio" % "0.0.1"
```

### scala cli

```scala
//> using lib com.alterationx10::opinionated-zio:0.0.1
```

### mill

```scala
ivy"com.alterationx10::opinionated-zio:0.0.1"
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

case class ServiceImpl(p: Int, cfg: Config) extends Service derives AutoLayer

val implLayer: ZLayer[Int & Config, Nothing, ServiceImpl] =
  AutoLayer[ServiceImpl]

val superLayer: ZLayer[Int & Config, Nothing, Service] =
  AutoLayer.as[Service, ServiceImpl]


```