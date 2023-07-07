package test.opinions

import zio.*
import zio.mock.*
import zio.test.Assertion

extension [S, E: Tag, I, O: Tag](serviceMethod: Mock[S]#Effect[I, E, O])
  def expectWhen(o: O, i: I): Expectation[S]  =
    serviceMethod.apply(i.eqTo, o.expected)
  def expectWhenF(e: E, i: I): Expectation[S] =
    serviceMethod.apply(i.eqTo, e.expectedF)
  def expectWhen(o: Result[Any, E, O], i: Assertion[I]): Expectation[S] =
    serviceMethod.apply(i, o)
