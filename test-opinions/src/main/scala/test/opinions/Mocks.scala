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

  /** Reversed argument order of [[expectWhen]]
    * @param i
    *   Asserted input value
    * @param o
    *   Expected output value
    * @return
    */
  def whenExpect(i: I, o: O): Expectation[S] =
    expectWhen(o, i)

  /** For a given ZIO Mock Capability (serviceMethod), partially apply the input
    * as Assertion.equalTo
    * @param i
    *   Asserted input value
    */
  def when(i: I): Result[I, E, O] => Expectation[S] =
    serviceMethod.apply(i.eqTo, _)

  /** For a given ZIO Mock Capability (serviceMethod), apply the given values as
    * Expectation.failure/Assertion.equalTo
    * @param e
    *   Expected output failure value
    * @param i
    *   Asserted input value
    */
  def expectWhenF(e: E, i: I): Expectation[S] =
    serviceMethod.apply(i.eqTo, e.expectedF)

  /** Reversed argument order for [[expectWhenF]]
    * @param i
    *   Asserted input value
    * @param e
    *   Expected output failure value
    * @return
    */
  def whenExpectF(i: I, e: E): Expectation[S] =
    expectWhenF(e, i)

  /** For a given ZIO Mock Capability (serviceMethod), apply the given
    * Expectation Result/Assertion
    * @param o
    *   The expected output Result/Expectation
    * @param i
    *   The expected input assertion
    */
  def expectWhen(o: Result[Any, E, O], i: Assertion[I]): Expectation[S] =
    serviceMethod.apply(i, o)

  /** Reversed argument order for [[expectWhen]]
    * @param i
    *   The expected input assertion
    * @param o
    *   The expected output Result/Expectation
    * @return
    */
  def whenExpect(i: Assertion[I], o: Result[Any, E, O]): Expectation[S] =
    expectWhen(o, i)

  /** For a given ZIO Mock Capability (serviceMethod), partially apply the input
    * assertion.
    *
    * @param i
    *   Input assertion
    */
  def when(i: Assertion[I]): Result[I, E, O] => Expectation[S] =
    serviceMethod.apply(i, _)

extension [S, E: Tag, I, O: Tag](
    partialServiceMethod: Result[I, E, O] => Expectation[S]
)

  /** Apply the argument as Expectation.value to the partially applied ZIO Mock
    * Capability
    * @param o
    *   The expected result value
    */
  def expect(o: O) = partialServiceMethod(o.expected)

  /** Apply the argument to the partially applied ZIO Mock Capability
    * @param o
    *   The expectation result
    * @return
    */
  def expect(o: Result[Any, E, O]) = partialServiceMethod(o)

  /** Apply the argument as Expectation.failure to the partially applied ZIO
    * Mock Capability
    * @param e
    *   The expected failure value
    * @return
    */
  def expectF(e: E) = partialServiceMethod(e.expectedF)
