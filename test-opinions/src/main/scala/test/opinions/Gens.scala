package test.opinions

import zio.test.*
import zio.*

object GenRandom:

  /** Generate a random List[A]
    * @param count
    *   The number of elements to generate
    * @param gen
    *   The implicit Gen[Any, A] to use.
    * @tparam A
    */
  def apply[A](count: Int)(using gen: Gen[Any, A]): UIO[List[A]] =
    gen.runCollectN(count)

  /** Generate a random A
    * @param gen
    *   The implicit Gen[Any, A] to use.
    * @tparam A
    */
  def apply[A](using gen: Gen[Any, A]): UIO[A] =
    GenRandom[A](1).map(_.head)
