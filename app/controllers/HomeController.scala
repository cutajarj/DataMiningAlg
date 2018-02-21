package controllers

import javax.inject._

import model.{Cluster, ClusterBatch, KMeansRequest, Point}
import play.api._
import play.api.mvc._
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Json}

import scala.util.Random

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  implicit val pointRead = Json.reads[Point]
  implicit val pointWrite = Json.writes[Point]

  implicit val kMeansRequestRead = Json.reads[KMeansRequest]
  implicit val kMeansRequestWrite = Json.writes[KMeansRequest]

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
    println(json)
    val kMeansRequest = json.as[KMeansRequest]

    val steps = kMeans(kMeansRequest.points, kMeansRequest.numberOfGroups)
      .map(cBatch => cBatch.clusters.map(cluster => cluster.centriod +: cluster.points))
    steps
    val points = kMeansRequest.points
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

    val jsonSteps = Json.toJson(steps)
    println(jsonSteps)
    Ok(jsonSteps)
  }

  val rand = new Random()

  def initialClusters(allPoints: List[Point], numbClusters: Int): List[Cluster] =
    Random.shuffle(allPoints).take(numbClusters).map(Cluster(_))

  def reCenter(clusters: List[Cluster]): List[Cluster] = clusters.map { c =>
    val xAvg = c.points.map(_.x).sum / c.points.size
    val yAvg = c.points.map(_.y).sum / c.points.size
    c.copy(centriod = Point(xAvg, yAvg))
  }

  def assignment(allPoints: List[Point], clusters: List[Cluster]): List[Cluster] = {
    val pointsByCluster = allPoints.groupBy(p => clusters.minBy(c => c.centriod.distanceTo(p)))
    clusters.map(c => c.copy(points = pointsByCluster.getOrElse(c, Nil)))
  }

  def kMeans(allPoints: List[Point], numbClusters: Int): List[ClusterBatch] = {
    val steps = Stream.iterate(ClusterBatch(initialClusters(allPoints, numbClusters))) { pClusterBatch =>
      val assignedClusters = assignment(allPoints, pClusterBatch.clusters)
      val reCentered = reCenter(assignedClusters)
      ClusterBatch(reCentered)
    }

    steps.zipWithIndex.tail.takeWhile { case (cBatch, index) =>
      cBatch.clusters.map(_.centriod) != steps(index - 1).clusters.map(_.centriod)
    }.map(_._1).toList
  }
}
