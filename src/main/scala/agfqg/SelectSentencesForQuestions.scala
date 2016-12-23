package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import breeze.linalg.DenseVector
import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object SelectSentencesForQuestions {

  type Vec = DenseVector[Double]

  import cmd.RunnerHelpers._
  import sutils.fp.ImplicitDisjunctionOps._

  lazy val argHelp: Array[String] => Boolean =
    args => args.size == 1 && (args.head == "-h" || args.head == "--help")

  val nExpectedArgs = 4
  def main(args: Array[String]): Unit = {
    if (args.size < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT threshold for selection
           |2nd: scored sentences file
           |3rd: original sentence data, non-processed
           |4th: OUTPUT selected sentences with score, sentence index, and original text
         """.stripMargin
      )
      System.exit(1)
    }

    val scoreTheshold: Double = args(0).toDouble
    log(s"Input, score theshold for:         $scoreTheshold")
    val scoredSentencesFi = new File(args(1))
    log(s"Input, scored sentence file:       $scoredSentencesFi")
    val originalSentenceFi = new File(args(2))
    log(s"Input, original indexed sentences: $originalSentenceFi")
    val outSelectedSentenceFi = new File(args(3))
    log(s"Output, selected sentences:        $outSelectedSentenceFi")

    val scoredSentences =
      readScoredSentences(scoredSentencesFi, originalSentenceFi).getUnsafe

    val w = new BufferedWriter(new FileWriter(outSelectedSentenceFi))

    var nWrote = 0
    var n = 0
    scoredSentences.foreach {
      case SelectedSentence(globalSentenceIndex, score, topTopics, sentence) =>
        n += 1
        if (score >= scoreTheshold && !sentence.unwrap.contains("?")) {
          w.write(s"$globalSentenceIndex\t")
          w.write(s"$score\t")
          w.write(topTopics.mkString(" ") + "\t")
          w.write(sentence.unwrap)
          w.newLine()
          nWrote += 1
        }
    }
    w.close()

    log(
      s"Passed $nWrote out of $n scored sentences, ~${math.round(nWrote.toDouble / n * 100.0).toInt}%")
    log(s"Success")
  }

  case class SelectedSentence(
      index: Int,
      score: Double,
      topTopics: Seq[Int],
      text: SentenceText
  )

  def readScoredSentences(
      scoredSentencesFi: File,
      originalSentenceFi: File
  ): Err[Iterator[SelectedSentence]] =
    \/.fromTryCatchNonFatal {

      val bothScoreTopTopics =
        Source.fromFile(scoredSentencesFi).getLines().map { line =>
          val bits = line.split(" ")
          val score = bits(0).toDouble
          val topTopics = bits.slice(1, bits.length).map { _.toInt }.toSeq
          (score, topTopics)
        }

      val originalSentences =
        Source.fromFile(originalSentenceFi).getLines()

      bothScoreTopTopics.zip(originalSentences).zipWithIndex.map {
        case (((score, topTopics), sentence), i) =>
          SelectedSentence(i, score, topTopics, sentence.wrap)
      }
    }

}
