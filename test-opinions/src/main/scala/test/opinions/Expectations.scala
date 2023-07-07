package test.opinions

import zio.*
import zio.mock.*

extension [A: Tag](a: A)

  /** Wrap a: A in Expectation.value(a)
    */
  def expected: Result[Any, Nothing, A] = Expectation.value(a)

  /** Wrap a: A in Expectation.failure(a)
    */
  def expectedF: Result[Any, A, Nothing] = Expectation.failure(a)
