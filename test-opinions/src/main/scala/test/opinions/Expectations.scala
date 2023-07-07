package test.opinions

import zio.*
import zio.mock.*

extension [A: Tag](a: A)
  def expected: Result[Any, Nothing, A]  = Expectation.value(a)
  def expectedF: Result[Any, A, Nothing] = Expectation.failure(a)
