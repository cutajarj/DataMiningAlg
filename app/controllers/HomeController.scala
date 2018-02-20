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
    val flippedPoints1 = points.map(p => Point(p.y, p.y))
    val flippedPoints2 = points.map(p => Point(p.x, p.x))
    val flippedPoints3 = points.map(p => Point(p.y, p.x))
    println(points)
    val jsonFlipped = Json.toJson(List(points, flippedPoints1, flippedPoints2, flippedPoints3))
    println(jsonFlipped)
    Ok(jsonFlipped)
  }

}
