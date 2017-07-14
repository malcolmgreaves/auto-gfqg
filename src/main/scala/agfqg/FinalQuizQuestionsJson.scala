package agfqg

import agfqg.SelectSentencesForQuestions.SelectedSentence

import java.io.{BufferedWriter, File, FileWriter}

import cmd.RunnerHelpers.log

import scala.io.Source

import rapture.json._
import jsonBackends.jawn._

object FinalQuizQuestionsJson {

  lazy val argHelp: Array[String] => Boolean =
    args => args.length == 1 && (args.head == "-h" || args.head == "--help")

  val nExpectedArgs = 3

  def main(args: Array[String]): Unit = {

    if (args.length < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT selected gaps & generated distractors
           |2nd: INPUT selected sentences
           |3rd: OUTPUT JSON formatted, complete, quiz questions
         """.stripMargin
      )
      System.exit(1)
    }

    val gapDistractorFi = new File(args.head)
    log(s"Input, selected gaps & generated distractors:     $gapDistractorFi")
    val selectSentFi = new File(args(1))
    log(s"Input, selected sentences:                        $selectSentFi")
    val outFi = new File(args(2))
    log(s"Output, JSON formatted, complete, quiz questions: $outFi")

    val qus = quizes(selectSentFi, gapDistractorFi)
    log(s"Assembled ${qus.size} quizzes.")
    writeAsJson(outFi, qus)
  }

  def writeAsJson(out: File, quizzes: Iterable[FormattedQuiz]): Unit = {
    val w = new BufferedWriter(new FileWriter(out))
    try {
      val j = Json(quizzes.toIndexedSeq)
      import formatters.compact._
      w.write(Json.format(j))
    } finally {
      w.close()
    }
  }

  def quizes(selectSentFi: File,
             gapDistractorFi: File): Iterable[FormattedQuiz] = {
    val selectedSentences = Source
      .fromFile(selectSentFi)
      .getLines()
      .map { parseSelectedSentenceLine }
      .map { x =>
        (x.index, x)
      }
      .toMap

    val gapAndDistractors = Source
      .fromFile(gapDistractorFi)
      .getLines()
      .map { parseGapAndDistractorsLine }
      .toSeq

    gapAndDistractors.toList.sortBy { _.index }.map {
      case GapAndDistractors(index, gap, gapCharacterIndicies, distractors) =>
        val selectedSentence = selectedSentences(index)
        val (formattedQuizText, newEndIndex) =
          gapReplace(SentenceText(selectedSentence.text), gapCharacterIndicies)
        FormattedQuiz(
          index = index,
          questionText = formattedQuizText,
          replacementStartIndex = gapCharacterIndicies._1,
          replacementEndIndex = newEndIndex,
          answer = gap,
          distractors = distractors
        )
    }
  }

  def parseSelectedSentenceLine(s: String): SelectedSentence = {
    /*
         FORMAT of selected sentences:
        <sentence_index>\t<score>\t<topic1> <topic2> <topic3>\t<original_sentence_text>\n

        EXAMPLE
        1       0.47795541999999996     9 6 11  21 Attacks against ATM, POS, and mobile....
     */
    val bits = s.split("\t")
    SelectedSentence(
      index = bits.head.toInt,
      score = bits(1).toDouble,
      topTopics = bits(2).split(" ").map { _.toInt }.toSeq,
      text = SentenceText(bits(3))
    )
  }

  def parseGapAndDistractorsLine(s: String): GapAndDistractors = {
    /*
        FORMAT of gap & distractors:
        <sentence_index>\t<gap>\t<gap_character_start_index> <gap_charater_end_index_plus_one>\t<distractor1> <distractor2> <distractor3> <distractor4>\n

        EXAMPLE
        2       criminals       573 582 issues billing investigation unauthorised
     */
    val bits = s.split("\t")
    GapAndDistractors(
      index = bits.head.toInt,
      gap = bits(1),
      gapCharacterIndicies = {
        val cbits = bits(2).split(" ")
        (cbits.head.toInt, cbits(1).toInt)
      },
      distractors = bits(3).split(" ")
    )
  }

  def gapReplace(text: String,
                 gapCharacterIndicies: (Int, Int),
                 replacement: String = "_____"): (String, Int) = {
    val (start, end) = gapCharacterIndicies
    val upToBeforeStart = text.substring(0, start)
    val atAndAfterEnd = text.substring(end)
    (
      s"$upToBeforeStart$replacement$atAndAfterEnd",
      start + replacement.length
    )
  }

}

case class GapAndDistractors(
    index: Int,
    gap: String,
    gapCharacterIndicies: (Int, Int),
    distractors: Seq[String]
)

case class FormattedQuiz(
    index: Int,
    questionText: String,
    replacementStartIndex: Int,
    replacementEndIndex: Int,
    answer: String,
    distractors: Seq[String]
)
