package opinions

import scala.compiletime.*
import scala.deriving.*
import scala.quoted.*
import scala.deriving.Mirror.ProductOf
import com.typesafe.config.ConfigFactory
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.{ReadError, toKebabCase}
import zio.config.typesafe.TypesafeConfig
import zio.*

import scala.annotation.targetName

extension [Z: Tag](z: Z)
  /** Wrap an instance z: Z in ZLayer.succeed
    */
  def ulayer: ULayer[Z] = ZLayer.succeed(z)

  /** Wrap an instance z: Z in ZIO.succeed
    * @return
    */
  def uio: UIO[Z] = ZIO.succeed(z)

extension [R: Tag, E: Tag, A: Tag](zio: ZIO[R, E, A])
  /** Wraps a zio: ZIO[R, E, A] as ZLayer(zio)
    */
  def zlayer: ZLayer[R, E, A] = ZLayer(zio)

object ConfigLayer:
  /** Map a path from a typesafe config to case class C
    */
  def apply[C: Descriptor: Tag](path: String): Layer[ReadError[String], C] =
    implicit lazy val configDescriptor: _root_.zio.config.ConfigDescriptor[C] =
      descriptor[C].mapKey(toKebabCase)
    TypesafeConfig
      .fromTypesafeConfig(
        ZIO
          .attempt(ConfigFactory.load().getConfig(path)),
        configDescriptor
      )

trait AutoLayer[A]:
  def zlayer(using
      p: Mirror.ProductOf[A]
  ): ZLayer[IAnyType[p.MirroredElemTypes], Nothing, A]

object AutoLayer:

  def apply[A](using l: AutoLayer[A])(using
      p: Mirror.ProductOf[A]
  ): ZLayer[IAnyType[p.MirroredElemTypes], Nothing, A] = l.zlayer

  def as[B, A <: B](using l: AutoLayer[A])(using
      p: Mirror.ProductOf[A]
  ): ZLayer[IAnyType[p.MirroredElemTypes], Nothing, B] = l.zlayer

  inline given derived[A](using m: Mirror.Of[A]): AutoLayer[A] =
    inline m match
      case _: Mirror.SumOf[A]     =>
        error("Auto derivation is not supported for Sum types")
      case p: Mirror.ProductOf[A] =>
        val services =
          listOfServices[p.MirroredElemTypes]

        val init: ZIO[IAnyType[p.MirroredElemTypes], Nothing, List[
          UAnyType[p.MirroredElemTypes]
        ]] = ZIO.succeed(List.empty)

        val flattened =
          services.foldLeft(init)((l, z) =>
            l.flatMap(_l => z.map(_z => _l :+ _z)),
          )

        val a: ZIO[IAnyType[p.MirroredElemTypes], Nothing, A] =
          flattened.map(deps => p.fromProduct(Tuple.fromArray(deps.toArray)))

        new AutoLayer[A]:
          override def zlayer(using
              pp: ProductOf[A]
          ): ZLayer[IAnyType[pp.MirroredElemTypes], Nothing, A] = ZLayer {
            a.asInstanceOf[ZIO[IAnyType[pp.MirroredElemTypes], Nothing, A]]
          }
