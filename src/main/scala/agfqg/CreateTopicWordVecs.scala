package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import breeze.linalg.DenseVector
import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object CreateTopicWordVecs {

  type Vec = DenseVector[Double]

  import cmd.RunnerHelpers._
  import sutils.fp.ImplicitDisjunctionOps._

  lazy val argHelp: Array[String] => Boolean =
    args => args.size == 1 && (args.head == "-h" || args.head == "--help")

  val nExpectedArgs = 5

  def main(args: Array[String]): Unit = {
    if (args.size < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT word vector file, in text format
           |2nd: BTM topic model conditional word probabilities (*.pw_z)
           |3rd: BTM topic model vocabulary file
           |4th: number of topic words to take
           |5th: OUTPUT weighted topic vectors
         """.stripMargin
      )
      System.exit(1)
    }

    val wordVectorFi = new File(args(0))
    log(s"Input, word vector file (text format):  $wordVectorFi")
    val btmPwzFi = new File(args(1))
    log(s"Input, conditional word probabilites:   $btmPwzFi")
    val btmVocabFi = new File(args(2))
    log(s"Input, vocabulary file:                 $btmVocabFi")
    val nWordsPerTopic = args(3).toInt
    log(s"Input, # of top words for each topic:   $nWordsPerTopic")
    val outWeightTopicVectorFi = new File(args(4))
    log(s"Output, weighted topic vector file:     $outWeightTopicVectorFi")

    val word2vec = readAndParseWordVectors(
      WordSimplifier.mut_porterStemmerCoreNlp,
      wordVectorFi).getUnsafe

    val bothWordAndProbability = readTopWordsPerTopic(
      WordSimplifier.mut_porterStemmerCoreNlp,
      btmPwzFi,
      btmVocabFi).getUnsafe

    val w = new BufferedWriter(new FileWriter(outWeightTopicVectorFi))

    bothWordAndProbability.foreach { topWords =>
      val tv = createTopicVector(word2vec, nWordsPerTopic, topWords)
      w.write(tv.data.mkString(" "))
      w.newLine()
    }

    w.close()
    log(s"Success")
  }

  def readAndParseWordVectors(s: WordSimplifier,
                              fi: File): Err[Map[String, Vec]] =
    \/.fromTryCatchNonFatal {
      val iter = Source.fromFile(fi).getLines()
      val _ = iter.next() // ignore first line, don't need # vecs nor dimensionality
      val bothWordVecs =
        iter.map { line =>
          val bits = line.split(" ")
          val word = s.simplify(bits.head)
          val v = DenseVector(bits.slice(1, bits.length).map { _.toDouble })
          (word, v)
        }

      bothWordVecs.foldLeft(Map.empty[String, Vec]) {
        case (m, (word, vec)) =>
          if (m contains word) {
            val existing = m(word)
            val combined = existing + vec
            (m - word) + (word -> combined)
          } else
            m + (word -> vec)
      }
    }

  def readTopWordsPerTopic(
      s: WordSimplifier,
      btmPwzFi: File,
      btmVocabFi: File): Err[IndexedSeq[Iterable[(String, Double)]]] =
    \/.fromTryCatchNonFatal {

      val vocabularyStemmed: IndexedSeq[String] = Source
        .fromFile(btmVocabFi)
        .getLines()
        .map { line =>
          s.simplify(line.split("\t")(1))
        }
        .toIndexedSeq

      val conditionalWordDistributions: IndexedSeq[Array[Double]] =
        Source
          .fromFile(btmPwzFi)
          .getLines()
          .map { line =>
            line.split(" ").map { _.toDouble }
          }
          .toIndexedSeq

      conditionalWordDistributions.map { topicDistOverWords =>
        vocabularyStemmed.zip(topicDistOverWords).sortBy {
          case (_, probability) => -probability
        }
      }
    }

  def createTopicVector(word2vec: Map[String, Vec],
                        nWordsPerTopic: Int,
                        topWords: Iterable[(String, Double)]): Vec =
    topWords.flatMap {
      case (word, probability) =>
        word2vec.get(word).map { v =>
          (v, probability)
        }
    }.take(nWordsPerTopic).map { case (v, p) => v :* p }.reduce[Vec] {
      case (wv1, wv2) => wv1 + wv2
    }
}
