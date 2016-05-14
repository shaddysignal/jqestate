package com.github.signal2564

import io.circe.{Encoder, Json}

class CountryInfoEncoder extends Encoder[CountryInfo] {
  override def apply(a: CountryInfo): Json = a.stuff
}
