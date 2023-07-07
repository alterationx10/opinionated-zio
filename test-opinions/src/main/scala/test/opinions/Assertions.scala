package test.opinions

import zio.*
import zio.test.*
import opinions.*

extension[A](a: A)
  def eqTo: Assertion[A] = Assertion.equalTo(a)
  def neqTo: Assertion[A] = Assertion.not(a.eqTo)