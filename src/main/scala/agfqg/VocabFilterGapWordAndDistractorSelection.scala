package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import agfqg.CreateTopicWordVecs._
import agfqg.ScoreSentences.weightsForTopTopics
import agfqg.SelectSentencesForQuestions.SelectedSentence
import breeze.linalg.DenseVector
import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object VocabFilterGapWordAndDistractorSelection {

  import cmd.RunnerHelpers._
  import sutils.fp.ImplicitDisjunctionOps._
  import AppHelpers._
  import agfqg.GapWordAndDistractorSelection._

  def main(args: Array[String]): Unit = {
    val nExpectedArgs = 5
    if (args.length < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT weighted topic vectors
           |2nd: INPUT selected sentences with score, sentence index, and original text
           |3rd: INPUT word vector file, in text format
           |4th: INPUT vocabulary file
           |5th: OUTPUT selected gap words and distractors per sentence
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
    val outGapDistractorFi = new File(args(4))
    log(s"Output, selected gap words & distractors: $outGapDistractorFi")

    val topicVectors = readTopicVectors(weightTopicVectorFi).getUnsafe

    val selectedSentences = readSelectedSentences(selectedSentenceFi).getUnsafe

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

      selectGapAndDistractors(
        stemmer,
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

}
