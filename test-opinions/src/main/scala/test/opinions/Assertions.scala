package test.opinions

import zio.*
import zio.test.*
import opinions.*

extension [A](a: A)

  /** Wrap a: A in Assertion.equalTo(a)
    */
  def eqTo: Assertion[A] = Assertion.equalTo(a)

  /** Wrap a: A in Assertion.not(Assertion.equalTo(a))
    */
  def neqTo: Assertion[A] = Assertion.not(a.eqTo)
