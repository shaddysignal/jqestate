package com.github

import io.circe.Json

package object signal2564 {
  implicit val countryInfoDecoder = new CountryInfoDecoder()
  implicit val countryInfoEncoder = new CountryInfoEncoder()

  case class Package(items: Array[CountryInfo], pagination: Pagination)

  case class CountryInfo(id: Int, state: String, stuff: Json)

  case class Pagination(total: Long, limit: Int, offset: Long)

  case class Filter(ids: Array[Int] = Array(), states: Array[String] = Array())
}