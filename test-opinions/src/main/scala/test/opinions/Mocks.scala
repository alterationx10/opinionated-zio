package test.opinions

import zio.*
import zio.mock.*
import zio.test.Assertion

extension [S, E: Tag, I, O: Tag](serviceMethod: Mock[S]#Effect[I, E, O])

  /** For a given ZIO Mock Capability (serviceMethod), apply the given values as
    * Expectation.value/Assertion.equalTo
    * @param o
    *   Expected output value
    * @param i
    *   Asserted input value
    */
  def expectWhen(o: O, i: I): Expectation[S] =
    serviceMethod.apply(i.eqTo, o.expected)

  /** For a given ZIO Mock Capability (serviceMethod), apply the given values as
    * Expectation.failure/Assertion.equalTo
    * @param e
    *   Expected output failure value
    * @param i
    *   Asserted input value
    */
  def expectWhenF(e: E, i: I): Expectation[S] =
    serviceMethod.apply(i.eqTo, e.expectedF)

  /** For a given ZIO Mock Capability (serviceMethod), apply the given
    * Expectation Result/Assertion
    * @param o
    *   The expected output Result/Expectation
    * @param i
    *   The expected input assertion
    */
  def expectWhen(o: Result[Any, E, O], i: Assertion[I]): Expectation[S] =
    serviceMethod.apply(i, o)
