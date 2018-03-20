import scala.util.Random

case class Point(x: Int, y: Int) {
  def distanceTo(other: Point) =
    math.sqrt(math.pow(other.x - this.x, 2) + math.pow(other.y - this.y, 2))
}
case class Cluster(centroid: Point, points: List[Point] = Nil)
case class ClusterBatch(clusters: List[Cluster])

def kMeans(allPoints:List[Point], n:Int):List[ClusterBatch] = {
  val initialBatch = ClusterBatch(initialClusters(allPoints, n))
  val steps = Stream.iterate(initialBatch){clusterBatch =>
    val assignedClusters = assignment(allPoints, clusterBatch.clusters)
    val reCentered = reCenter(assignedClusters)
    ClusterBatch(reCentered)
  }

  steps.zipWithIndex.takeWhile{ case (cBatch, index) =>
      cBatch.clusters.map(_.centroid) != steps(index-1).clusters.map(_.centroid)
  }.map{case (cBatch, index) => cBatch}.toList
}

def assignment(allPoints: List[Point], clusters: List[Cluster]): List[Cluster] = {
  val ptsByCluster = allPoints.groupBy(p => clusters.minBy(_.centroid.distanceTo(p)))
  clusters.map(c => c.copy(points = ptsByCluster.getOrElse(c, Nil)))
}

def initialClusters(allPts: List[Point], n: Int): List[Cluster] =
  Random.shuffle(allPts).take(n).map(pt => Cluster(pt))

def reCenter(clusters: List[Cluster]): List[Cluster] = clusters.map { c =>
  val xAvg = c.points.map(_.x).sum / c.points.size
  val yAvg = c.points.map(_.y).sum / c.points.size
  c.copy(centroid = Point(xAvg, yAvg))
}

