package test.opinions

import zio.test.*
import zio.*

object GenRandom:
  def apply[A](count: Int)(using gen: Gen[Any, A]): UIO[List[A]] =
    gen.runCollectN(count)

  def apply[A](using gen: Gen[Any, A]): UIO[A] =
    GenRandom[A](1).map(_.head)
