package controllers

import javax.inject._

import model.Point
import play.api._
import play.api.mvc._
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Json}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  implicit val pointRead = Json.reads[Point]
  implicit val pointWrite = Json.writes[Point]

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def computeKmeans = Action { request =>
    val json = request.body.asJson.get
    val points = json.as[List[Point]]
    val points2 = points.map(p => Point(p.x + 2, p.y + 2))
    val points3 = points.map(p => Point(p.x - 2, p.y + 2))

    val flippedPoints1 = points.map(p => Point(p.y, p.y))
    val flippedPoints12 = points.map(p => Point(p.y + 2, p.y + 2))
    val flippedPoints13 = points.map(p => Point(p.y - 2, p.y + 2))

    val flippedPoints2 = points.map(p => Point(p.x, p.x))
    val flippedPoints22 = points.map(p => Point(p.x + 2, p.x + 2))
    val flippedPoints23 = points.map(p => Point(p.x - 2, p.x + 2))

    val flippedPoints3 = points.map(p => Point(p.y, p.x))
    val flippedPoints32 = points.map(p => Point(p.y + 2, p.x + 2))
    val flippedPoints33 = points.map(p => Point(p.y - 2, p.x + 2))

    println(points)
    val jsonFlipped = Json.toJson(List(
      List(points, points2),
      List(flippedPoints1, flippedPoints12),
      List(flippedPoints2, flippedPoints22),
      List(flippedPoints3, flippedPoints32)))
    println(jsonFlipped)
    Ok(jsonFlipped)
  }

}
