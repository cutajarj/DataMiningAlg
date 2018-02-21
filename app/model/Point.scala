package model

case class Point(x: Int, y: Int) {
  def distanceTo(other: Point): Double =
    math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2))
}
