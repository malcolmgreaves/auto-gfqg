package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import agfqg.SelectSentencesForQuestions.SelectedSentence
import agfqg.ScoreSentences.weightsForTopTopics
import agfqg.CreateTopicWordVecs._
import breeze.linalg.DenseVector
import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object GapWordAndDistractorSelection {

  import cmd.RunnerHelpers._
  import sutils.fp.ImplicitDisjunctionOps._
  import AppHelpers._

  def main(args: Array[String]): Unit = {
    val nExpectedArgs = 4
    if (args.length < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT weighted topic vectors
           |2nd: INPUT selected sentences with score, sentence index, and original text
           |3rd: INPUT word vector file, in text format
           |4th: OUTPUT selected gap words and distractors per sentence
         """.stripMargin
      )
      System.exit(1)
    }

    val weightTopicVectorFi = new File(args(0))
    log(s"Input, weighted topic vector file:        $weightTopicVectorFi ")
    val selectedSentenceFi = new File(args(1))
    log(s"Input, selected sentences:                $selectedSentenceFi ")
    val wordVectorFi = new File(args(2))
    log(s"Input, word vector file (text format):    $wordVectorFi")
    val outGapDistractorFi = new File(args(3))
    log(s"Output, selected gap words & distractors: $outGapDistractorFi")

    val topicVectors = readTopicVectors(weightTopicVectorFi).getUnsafe

    val selectedSentences = readSelectedSentences(selectedSentenceFi).getUnsafe

    val bothGapDistractorSelector = {

      val stemmedWord2vec = readAndParseWordVectors(
        WordSimplifier.mut_porterStemmerCoreNlp,
        wordVectorFi).getUnsafe

      val word2vec =
        readAndParseWordVectors(WordSimplifier.noop, wordVectorFi).getUnsafe.filter {
          case (word, _) => !stopWords.contains(word)
        }.toIndexedSeq

      selectGapAndDistractors(
        WordSimplifier.mut_porterStemmerCoreNlp,
        Tokenizer.ptbTokenizer,
        Distance.cosineSimilarity,
        stemmedWord2vec,
        topicVectors,
        LanguageModel.english,
        word2vec,
        nDistractors = 4
      ) _
    }

    val w = new BufferedWriter(new FileWriter(outGapDistractorFi))

    var n = 0
    var nWrote = 0
    selectedSentences.foreach { sentence =>
      n += 1
      bothGapDistractorSelector(sentence).foreach {
        case BothGapDistractors(gapWord, range, distractors) =>
          w.write(s"${sentence.index}\t")
          w.write(s"$gapWord\t")
          w.write(s"${range.start} ${range.end}\t")
          w.write(distractors.mkString(" ") + "\t")
          w.newLine()
          nWrote += 1
          if (nWrote == 1 || nWrote % 10 == 0) {
            w.flush()
          }
      }
    }
    w.close()
    log(
      s"Generated gaps & distractors for $nWrote out of $n scored sentences, ~${math.round(nWrote.toDouble / n * 100.0).toInt}%"
    )

    log(s"Success")
  }

  def readSelectedSentences(
      selectedSentenceFi: File): Err[Iterator[SelectedSentence]] =
    \/.fromTryCatchNonFatal {
      Source.fromFile(selectedSentenceFi).getLines().map { line =>
        val bits = line.split("\t")
        SelectedSentence(
          index = bits(0).toInt,
          score = bits(1).toDouble,
          topTopics = bits(2).split(" ").map { _.toInt },
          text = bits(3).wrap
        )
      }
    }

  def readTopicVectors(weightTopicVectroFi: File): Err[IndexedSeq[Vec]] =
    \/.fromTryCatchNonFatal {
      Source
        .fromFile(weightTopicVectroFi)
        .getLines()
        .map { line =>
          DenseVector(line.split(" ").map { _.toDouble })
        }
        .toIndexedSeq
    }

  case class BothGapDistractors(
      gapWord: String,
      gapIndices: Range,
      distractors: Seq[String]
  )

  val (wGapVecNormal, wSentTopicVecNormal) = (0.6, 0.4)

  def selectGapAndDistractors(
      s: WordSimplifier,
      t: Tokenizer,
      d: Distance,
      stemmedWord2vec: Map[String, Vec],
      topicVectors: IndexedSeq[Vec],
      lm: LanguageModel,
      wordVectors: Seq[(String, Vec)],
      nDistractors: Int
  )(sentence: SelectedSentence): Option[BothGapDistractors] = {

    val weightedSentenceTopicVector: Vec =
      sentence.topTopics.map { topicVectors.apply }
        .zip(weightsForTopTopics)
        .map { case (v, w) => v :* w }
        .reduce[Vec] { _ + _ }

    val tokens = t.tokenizeWithStarts(sentence.text.unwrap)

    val stemsThatOccurMoreThanOnceInSentence: Set[String] =
      tokens
        .foldLeft(Map.empty[String, Int]) {
          case (m, (raw, _)) =>
            val stem = s.simplify(raw)
            if (m contains stem)
              (m - stem) + (stem -> (m(stem) + 1))
            else
              m + (stem -> 1)
        }
        .filter { case (_, count) => count > 1 }
        .map { case (word, _) => word }
        .toSet

    val gapCandidatesByDist =
      tokens.flatMap {
        case (raw, startIndex) =>
          val stem = s.simplify(raw)
          val isUnique = !stemsThatOccurMoreThanOnceInSentence.contains(stem)
          val isStop = stopWords.contains(raw)

          if (isUnique && !isStop)
            stemmedWord2vec.get(stem).map { vec =>
              (raw, startIndex, vec)
            } else
            None
      }.sortBy {
        case (_, _, gapVec) =>
          -d.distance(gapVec, weightedSentenceTopicVector)
      }

    val recombineForSent = recombine(sentence.text) _

    gapCandidatesByDist.headOption.map {
      case (gapWord, itsStartingIndex, gapVec) =>
        val gapIndices = Range(
          start = itsStartingIndex,
          end = itsStartingIndex + gapWord.length,
          step = 1
        )

        val recombineForGap = recombineForSent(gapIndices)

        val distractors = {

          val stemsInSentence = tokens.map {
            case (word, _) => s.simplify(word).toLowerCase
          }.toSet

          val combinedReWeightedGapSentTopicVec =
            (gapVec :* wGapVecNormal) + (weightedSentenceTopicVector :* wSentTopicVecNormal)

          val distractorCandidatesCloseToGap = wordVectors.filter {
            case (raw, _) =>
              val stem = s.simplify(raw)
              !stemsInSentence.contains(stem)
          }.sortBy {
            case (_, vec) =>
              -d.distance(combinedReWeightedGapSentTopicVec, vec)
          }.map { case (word, _) => word }.take(math.max(nDistractors, 50))

          distractorCandidatesCloseToGap.sortBy { distractorCandidate =>
            -lm.probabilityOf(recombineForGap(distractorCandidate))
          }.take(nDistractors)
        }

        BothGapDistractors(
          gapWord = gapWord,
          gapIndices,
          distractors
        )
    }
  }

  def recombine(text: SentenceText)(gap: Range)(part: String): String = {
    val (beforeGap, afterGap) = {
      val b = text.unwrap.substring(0, gap.start)
      val a = text.unwrap.substring(math.min(text.unwrap.length, gap.end))
      (b, a)
    }

    s"$beforeGap $part $afterGap".trim
  }

}
