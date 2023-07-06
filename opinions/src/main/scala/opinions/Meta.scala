package opinions

import scala.compiletime.*
import zio.*

/** An Intersection type, but with the default of Any if empty. Useful for
  * turning CaseClass(a: A, b: B, ...) into a type A & B & ... via a Mirror.
  */
type IAnyType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x & y]

/** An Intersection type, but with the default of Nothing if empty. Useful for
  * turning CaseClass(a: A, b: B, ...) into a type A & B & ... via a Mirror.
  */
type INothingType[T <: Tuple] = Tuple.Fold[T, Nothing, [x, y] =>> x & y]

/** A Union type, but with the default of Any if empty. Useful for turning
  * CaseClass(a: A, b: B, ...) into a type A | B | ... via a Mirror. Note that
  * the there is a built in scala.Union type for this which defaults to Nothing.
  */
type UAnyType[T <: Tuple] = Tuple.Fold[T, Any, [x, y] =>> x | y]

/** Summon type class instances from case class constructor arguments via
  * Mirrors. For example, for CaseClass(a: A, b: B) and TypeClass[T], we can use
  * this to summon a List[TypeClass[ A | B ]]
  */
inline def listOf[T <: Tuple, A[_]]: List[A[UAnyType[T]]] =
  _listOfAs[T, UAnyType[T], A]

/** A helper method for summonListOf, since the main operations is recursive for
  * T, and we would lose a type in the Tuple each time, we keep a version of it
  * constant - U.
  */
private inline def _listOfAs[T <: Tuple, U, A[U]]: List[A[U]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  =>
      summonInline[A[t]].asInstanceOf[A[U]] :: _listOfAs[ts, U, A]

/** Summon a List of (ZIO.service[?]) dependencies. Useful for auto deriving
  * ZLayers. For example, for case class Thing(a: A, b: B, c: C), we can
  * generate a List[URIO[A & B & C, A | B | C]] which can then be used to make a
  * ZLayer[A & B & C, Nothing, Thing]
  */
inline def listOfServices[T <: Tuple]: List[URIO[IAnyType[T], UAnyType[T]]] =
  _listOfServices[T, T]

/** A helper method for listOfServices, since the main operations is recursive
  * for T, and we would lose a type in the Tuple each time, we keep a version of
  * it constant - U.
  */
private inline def _listOfServices[T <: Tuple, U <: Tuple]
    : List[URIO[IAnyType[U], UAnyType[U]]] =
  inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (t *: ts)  =>
      ZIO.service[t].asInstanceOf[URIO[IAnyType[U], UAnyType[U]]] ::
        _listOfServices[ts, U]
