package com.github.signal2564

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.generic.auto._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

object Main extends App with CirceSupport {

  implicit val system = ActorSystem("main-system")
  implicit val materializer = ActorMaterializer()

  // For executing futures
  implicit val executionContext = system.dispatcher

  // Important constants
  val targetUrl = "https://api.jqestate.ru/v1/properties/country"
  val itemsPerPage = 256
  val awaitAtMost = 60 seconds

  val data = acquireStorage

  val route = path("v1" / "properties") {
    get {
      parameterMap(params => {
        val ofids = params.get("filter[id]").map(_.split(','))
        val ofstates = params.get("filter[state]").map(_.split(','))
        val poffset = params.getOrElse("pagination[offset]", "0").toInt
        val plimit = params.getOrElse("pagination[limit]", "32").toInt

        complete(
          Package(
            data.toStream.filter(el => {
              ofids.forall(_.contains(el.id.toString)) && ofstates.forall(_.contains(el.state))
            }).slice(poffset, poffset + plimit).toArray,
            Pagination(data.length, plimit, poffset)
          )
        )
      })
    }
  }

  Http().bindAndHandle(route, "localhost", 8080)

  private def acquireStorage: Array[CountryInfo] = {
    def fetchPage(limit: Long = itemsPerPage, offset: Long = 0): Future[Package] = {
      val paginationParams = Uri.Query(
        Map("pagination[limit]" -> limit.toString, "pagination[offset]" -> offset.toString)
      )

      Http().singleRequest(
        HttpRequest(uri = Uri(targetUrl).withQuery(paginationParams))
      ).map(response => Await.result(Unmarshal(response.entity).to[Package], awaitAtMost))
    }

    val source = Source.unfoldAsync(0) { offset =>
      val futurePackage = fetchPage(itemsPerPage, offset)

      futurePackage.map(pack => {
        val p = pack.pagination

        if (p.total < p.offset) None else Some((offset + itemsPerPage, pack.items))
      })
    }

    Await.result(source.runReduce((p1, p2) => p1 ++ p2), awaitAtMost)
  }
}
