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

  lazy val asOneLine: ConllSent => String = {
    val pipeline = (getWordsAsStrings _)
      .andThen(transformSpecialTokens)
      .andThen(mergeTokens)
      .andThen(properSpacing)
      .andThen(finalStringification)

    sentence =>
      if (sentence.tokens.isEmpty)
        ""
      else
        pipeline(sentence)
  }

  def getWordsAsStrings(s: ConllSent): Seq[String] =
    s.tokens.map { _.raw }

  lazy val transformSpecialTokens: Seq[String] => Seq[String] = {
    val specialTransformTo = Map(
      "-LRB-" -> "(",
      "-RRB-" -> ")",
      "-LSB-" -> "[",
      "-RSB-" -> "]",
      "-LCB-" -> "{",
      "-RCB-" -> "}"
    )

    _.map { raw =>
      specialTransformTo.getOrElse(raw, raw)
    }
  }

  lazy val mergeTokens: Seq[String] => Seq[String] = {

    def tokenPairMergeTest(current: String, next: String): Boolean =
      next == """n't""" || next == """'s""" ||
        current == "(" || current == "[" || current == "{"

    raws =>
      raws.zipWithIndex
        .foldLeft((List.empty[String], false)) {
          case ((s, justMerged), (raw, index)) =>
            val nextIndex = index + 1

            if (justMerged) {
              // we just merged our current `raw` with the previous one,
              // --> skip
              (s, false)

            } else if (nextIndex < raws.size && tokenPairMergeTest(
                         raw,
                         raws(nextIndex))) {
              (s :+ s"$raw${raws(nextIndex)}", true)

            } else {
              (s :+ raw, false)
            }
        }
        ._1
  }

  lazy val properSpacing: Seq[String] => Seq[String] = {
    val specialNoSepAfter = Set(",",
                                """$""",
                                ",",
                                "?",
                                "!",
                                ".",
                                """"""",
                                "'",
                                ")",
                                // no left round, square, or curly brackets:
                                // otherwise we'd have National Basketball Association( NBA)
                                // when we want National Basketball Association (NBA)
                                "]",
                                "}",
                                ":")

    _.map { mappedWord =>
      if (specialNoSepAfter.contains(mappedWord))
        mappedWord
      else
        " " + mappedWord
    }
  }

  def finalStringification(s: Seq[String]): String =
    s.mkString("").trim

}
