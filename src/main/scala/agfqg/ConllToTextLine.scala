package agfqg

import java.io.{BufferedWriter, File, FileWriter}

import cmd.RunnerHelpers.log

object ConllToTextLine {

  lazy val argHelp: Array[String] => Boolean =
    args => args.length == 1 && (args.head == "-h" || args.head == "--help")

  val nExpectedArgs = 2

  def main(args: Array[String]): Unit = {

    if (args.length < nExpectedArgs || argHelp(args)) {
      log(
        s"""[HELP] Need $nExpectedArgs arguments:
           |1st: INPUT processed text, in CoNLL format
           |2nd: OUTPUT plain text, each line is a complete sentence
         """.stripMargin
      )
      System.exit(1)
    }

    val processedFi = new File(args.head)
    log(s"Input, CoNLL-formatted:          $processedFi")
    val outFi = new File(args(1))
    log(s"Output, text sentence-per-line:  $outFi")
    val writingTextTransformer = transformText(outFi) _

    ConllFmt
      .reader(processedFi)
      .fold(
        e => throw e,
        writingTextTransformer
      )
  }

  def transformText(out: File)(iter: Iterator[ConllSent]): Unit = {
    val w = new BufferedWriter(new FileWriter(out))
    try {
      val nSentences = iter.foldLeft(0) {
        case (n, sentence) =>
          w.write(asOneLine(sentence))
          w.newLine()
          n + 1
      }
      w.flush()
      log(s"Found and wrote $nSentences sentences")
    } finally {
      w.close()
    }
  }

  def asOneLine(sentence: ConllSent): String =
    if (sentence.tokens.isEmpty)
      ""
    else {
      val rawWords = sentence.tokens.map { _.raw }
      val rws =
        if (rawWords.last == ".")
          rawWords.slice(0, rawWords.size - 1)
        else
          rawWords
      s"""${rws.mkString(" ")}."""
    }

}
