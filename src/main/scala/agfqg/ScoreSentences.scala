package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import scala.io.Source

object ScoreSentences {

  import cmd.RunnerHelpers._

  lazy val score: Distribution => Double =
    _.unwrap.sortBy { -_ }
      .take(weightsForTopTopics.size)
      .zip(weightsForTopTopics)
      .foldLeft(0.0) {
        case (sum, (w, p)) => sum + w * p
      }

  lazy val rankAndTake: Distribution => Seq[Int] =
    _.unwrap.zipWithIndex.sortBy { case (probability, _) => -probability }
      .take(weightsForTopTopics.size)
      .map { case (_, index) => index }

  lazy val weightsForTopTopics: Seq[Double] = Seq(0.5, 0.3, 0.2)

  lazy val parseDistLine: String => Distribution =
    _.split(" ").map { x =>
      try {
        x.toDouble
      } catch {
        case _: java.lang.NumberFormatException => 0.0
      }
    }.toSeq.wrap

  lazy val argHelp: Int => Array[String] => Boolean =
    expectedSize =>
      args =>
        args.size < expectedSize ||
          (args.size == 1 && (args.head == "-h" || args.head == "--help"))

  def main(args: Array[String]): Unit = {
    if (argHelp(2)(args)) {
      log(
        s"""[ERROR] Need 2 arguments
           |1st: INPUT *.pz_d file: topic distribution by sentence
           |2nd: OUTPUT scored and top 3 topic per sentence
         """.stripMargin
      )
      System.exit(1)
    }

    val pzdFi = new File(args.head)
    log(s"Input, conditional topic distribution by sentence: $pzdFi")
    val outScoreFi = new File(args(1))
    log(s"Output, sentence scores:                           $outScoreFi")

    val w = new BufferedWriter(new FileWriter(outScoreFi))
    Source.fromFile(pzdFi).getLines().foreach { line =>
      val d = parseDistLine(line)

      val s = score(d)
      w.write(s"$s ")

      val top3 = rankAndTake(d)
      w.write(top3.mkString(" "))

      w.newLine()
    }
    w.close()
  }

}
