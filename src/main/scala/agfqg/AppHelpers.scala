package agfqg

import breeze.linalg.DenseVector

object AppHelpers {

  type Vec = DenseVector[Double]

  lazy val argHelp: Array[String] => Boolean =
    args => args.length == 1 && (args.head == "-h" || args.head == "--help")

}
