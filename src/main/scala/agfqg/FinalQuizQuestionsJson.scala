package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import cmd.RunnerHelpers.log

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
    val selectedSentencesFi = new File(args(1))
    log(
      s"Input, selected sentences:                        $selectedSentencesFi")
    val outFi = new File(args(2))
    log(s"Output, JSON formatted, complete, quiz questions: $outFi")

    ???
  }

}
