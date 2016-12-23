package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import agfqg.CreateTopicWordVecs._
import agfqg.ScoreSentences.weightsForTopTopics
import agfqg.SelectSentencesForQuestions.SelectedSentence
import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object NlpAwareGapWordAndDistractorSelection {

  import cmd.RunnerHelpers._
  import sutils.fp.ImplicitDisjunctionOps._
  import AppHelpers._
  import agfqg.GapWordAndDistractorSelection._

  def main(args: Array[String]): Unit = {
    val nExpectedArgs = 6
    if (args.length < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT weighted topic vectors
           |2nd: INPUT selected sentences with score, sentence index (original text not necessary here)W
           |3rd: INPUT word vector file, in text format
           |4th: INPUT vocabulary file
           |5th: INPUT conll format of all sentences (align 1-1 w/ selected sentence indices)
           |6th: OUTPUT selected gap words and distractors per sentence
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
    val vocabFi = new File(args(3))
    log(s"Input, vocabulary:                        $vocabFi")
    val conllFmtSentencesFi = new File(args(4))
    log(s"Input, conll format, NLP'd sentences:     $conllFmtSentencesFi")
    val outGapDistractorFi = new File(args(5))
    log(s"Output, selected gap words & distractors: $outGapDistractorFi")

    val topicVectors = readTopicVectors(weightTopicVectorFi).getUnsafe

    val selectedConllSentences = readSelectedConllSentences(
      selectedSentenceFi,
      conllFmtSentencesFi).getUnsafe

    val bothGapDistractorSelector = {

      val stemmer = WordSimplifier.mut_porterStemmerCoreNlp

      val vocabAllLcase = Source
        .fromFile(vocabFi)
        .getLines()
        .map { _.split("\t")(1).toLowerCase }
        .toSet

      val vocabStem = vocabAllLcase.map { stemmer.simplify }

      val stemmedWord2vec =
        readAndParseWordVectors(stemmer, wordVectorFi).getUnsafe.filter {
          case (word, _) =>
            val unique = !stopWords.contains(word)
            val inVocab = vocabStem.contains(word)
            unique && inVocab
        }

      val word2vec =
        readAndParseWordVectors(WordSimplifier.noop, wordVectorFi).getUnsafe.filter {
          case (word, _) =>
            val unique = !stopWords.contains(word)
            val inVocab = vocabAllLcase.contains(word)
            unique && inVocab
        }.toIndexedSeq

      val posTagsOfWord =
        ConllFmt
          .reader(conllFmtSentencesFi)
          .getUnsafe
          .flatMap {
            _.tokens.map {
              case ConllWord(raw, _, posTag, _) =>
                (raw.toLowerCase, posTag)
            }
          }
          .foldLeft(Map.empty[String, Set[String]]) {
            case (m, (word, tag)) =>
              if (m contains word)
                (m - word) + (word -> (m(word) + tag))
              else
                m + (word -> Set(tag))
          }

      selectGapAndDistractorsConll(
        stemmer,
        Distance.cosineSimilarity,
        stemmedWord2vec,
        topicVectors,
        LanguageModel.english,
        word2vec,
        posTagsOfWord,
        nDistractors = 4
      ) _
    }

    val w = new BufferedWriter(new FileWriter(outGapDistractorFi))

    var n = 0
    var nWrote = 0
    selectedConllSentences.foreach { conllSent =>
      n += 1
      bothGapDistractorSelector(conllSent).foreach {
        case BothGapDistractors(gapWord, range, distractors) =>
          w.write(s"${conllSent.index}\t")
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

  case class SelectedScoredConllSentence(
      index: Int,
      score: Double,
      topTopics: Seq[Int],
      sentence: ConllSent,
      text: String
  )

  def readSelectedConllSentences(
      selectedScoredSentecnes: File,
      conllFmtSentencesFi: File
  ): Err[Iterator[SelectedScoredConllSentence]] =
    \/.fromTryCatchNonFatal {

      val mkIter4Scored =
        () => Source.fromFile(selectedScoredSentecnes).getLines()

      val sentencesToKeep = mkIter4Scored().map { line =>
        line.substring(0, line.indexOf("\t")).toInt
      }.toSet

      val conllSentences = ConllFmt
        .reader(conllFmtSentencesFi)
        .getUnsafe
        .zipWithIndex
        .filter {
          case (_, index) =>
            sentencesToKeep.contains(index)
        }
        .map { case (x, _) => x }

      val scoredSentences = mkIter4Scored().map { line =>
        val bits = line.split("\t")
        val globalSentenceIndex = bits(0).toInt
        val score = bits(1).toDouble
        val topTopics = bits(2).split(" ").map { _.toInt }.toSeq
        val text = bits(3)
        (globalSentenceIndex, score, topTopics, text)
      }

      scoredSentences.zip(conllSentences).map {
        case ((index, score, topTopics, text), sentence) =>
          SelectedScoredConllSentence(index, score, topTopics, sentence, text)
      }
    }

  val (wGapVecNlp, wSentTopicVecNlp) = (wGapVecNormal, wSentTopicVecNormal)

  def selectGapAndDistractorsConll(
      s: WordSimplifier,
      d: Distance,
      stemmedWord2vec: Map[String, Vec],
      topicVectors: IndexedSeq[Vec],
      lm: LanguageModel,
      wordVectors: Seq[(String, Vec)],
      posTagsOfWord: Map[String, Set[String]],
      nDistractors: Int
  )(sent: SelectedScoredConllSentence): Option[BothGapDistractors] = {

    val weightedSentenceTopicVector: Vec =
      sent.topTopics.map { topicVectors.apply }
        .zip(weightsForTopTopics)
        .map { case (v, w) => v :* w }
        .reduce[Vec] { _ + _ }

    val tokens = {

      val wStartingIndicies =
        Tokenizer.ptbTokenizer.tokenizeWithStarts(sent.text)

      val preProcessedTokens = sent.sentence.tokens

      preProcessedTokens.zip(wStartingIndicies).map {
        case (conllWord, (_, startingIndex)) => (conllWord, startingIndex)
      }
    }

    val wordsThatOccurMoreThanOnceInSentence: Set[String] =
      tokens
        .foldLeft(Map.empty[String, Int]) {
          case (m, (c, _)) =>
            val word = s.simplify(c.raw.toLowerCase)
            if (m contains word)
              (m - word) + (word -> (m(word) + 1))
            else
              m + (word -> 1)
        }
        .filter { case (_, count) => count > 1 }
        .map { case (word, _) => word }
        .toSet

    val gapCandidatesByDist =
      tokens.flatMap {
        case (c, itsStartingIndex) =>
          if (okPosTagsForGapOrDistractor.contains(c.posTag)) {
            val word = s.simplify(c.raw.toLowerCase)
            val isUnique = !wordsThatOccurMoreThanOnceInSentence.contains(word)
            val isStop = stopWords.contains(word)

            if (isUnique && !isStop)
              stemmedWord2vec.get(word).map { vec =>
                (c, itsStartingIndex, vec)
              } else
              None
          } else
            None

      }.sortBy {
        case (_, _, gapVec) =>
          -d.distance(gapVec, weightedSentenceTopicVector)
      }

    val recombineForSent = recombine(sent.text.wrap) _

    gapCandidatesByDist.headOption.flatMap {
      case (gapWord, itsStartingIndex, gapVec) =>
        val gapIndices = Range(
          start = itsStartingIndex,
          end = itsStartingIndex + gapWord.raw.length,
          step = 1
        )

        val recombineForGap = recombineForSent(gapIndices)

        val distractors = {

          val sentenceWords = tokens.map {
            case (word, _) =>
              word.raw.toLowerCase
          }.toSet

          val combinedReWeightedGapSentTopicVec =
            (gapVec :* wGapVecNlp) + (weightedSentenceTopicVector :* wSentTopicVecNlp)

          wordVectors.filter {
            case (word, _) =>
              val notInSentence = !sentenceWords.contains(word)
              val okPosTag = posTagsOfWord.get(word).exists {
                _.contains { gapWord.posTag }
              }

              notInSentence && okPosTag
          }.sortBy {
            case (distractorCandidate, _) =>
              -lm.probabilityOf(recombineForGap(distractorCandidate))
          }.take(nDistractors).map { case (distractor, _) => distractor }

//          val distractorCandidatesCloseToGap = wordVectors.filter {
//            case (word, _) =>
//              val notInSentence = !sentenceWords.contains(word)
//              val okPosTag = posTagsOfWord.get(word).exists {
//                _.contains { gapWord.posTag }
//              }
//
//              notInSentence && okPosTag
//
//          }.sortBy {
//            case (_, vec) =>
//              -d.distance(combinedReWeightedGapSentTopicVec, vec)
//          }.map { case (word, _) => word }.take(math.max(nDistractors, 50))
//
//          distractorCandidatesCloseToGap.sortBy { distractorCandidate =>
//            -lm.probabilityOf(recombineForGap(distractorCandidate))
//          }.take(nDistractors)
        }

        if (distractors.nonEmpty)
          Some {
            BothGapDistractors(
              gapWord = gapWord.raw,
              gapIndices,
              distractors
            )
          } else
          None
    }
  }

}
