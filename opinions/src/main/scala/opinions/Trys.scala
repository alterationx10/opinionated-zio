package opinions

import scala.util.Try

extension [A](a: Try[A])
  /** Maps the result of a Try[A] to be wrapped in Option.apply, converts the
    * resulting Try to an option, and then flattens the value. Useful on
    * Try-wrapped Java methods that successfully return null instead of throwing
    * an exception.
    */
  def safeOpt: Option[A] =
    a.map(Option.apply).toOption.flatten
