package agfqg

import breeze.linalg.DenseVector

trait Distance {
  def distance(a: DenseVector[Double], b: DenseVector[Double]): Double
}

object Distance {

  def magnitude(v: DenseVector[Double]): Double =
    math.sqrt(v.dot(v))

  lazy val cosineSimilarity = new Distance {
    override def distance(a: DenseVector[Double], b: DenseVector[Double]) =
      (a dot b) / (magnitude(a) * magnitude(b))
  }

}
