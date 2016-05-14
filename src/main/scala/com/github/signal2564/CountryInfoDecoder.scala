package com.github.signal2564

import cats.data.Xor
import io.circe.Decoder.Result
import io.circe._

class CountryInfoDecoder extends Decoder[CountryInfo] {
  override def apply(c: HCursor): Result[CountryInfo] = {
    val idResult = c.get[Int]("id")
    val stateResult = c.get[String]("state")

    (idResult, stateResult) match {
      case (Xor.Right(id), Xor.Right(state)) =>
        Xor.right(CountryInfo(id, state, c.focus))
      case (_, _) =>
        Xor.left(DecodingFailure("Does not have id or state", List()))
    }
  }
}