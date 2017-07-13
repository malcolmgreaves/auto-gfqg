package agfqg

import java.io.File

import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object ConllFmt {

  /** Produces an Iterator with **MUTABLE INTERNAL STATE** Not threadsafe!. */
  def reader(fi: File): Err[Iterator[ConllSent]] =
    \/.fromTryCatchNonFatal {
      val text = Source.fromFile(fi).mkString
      new Iterator[ConllSent] {

        var indexOfLastSentenceEnd: Int = 0
        var indexOfNextSentenceEnd: Int = text.indexOf("\n\n", 0)

        def isActive() = indexOfLastSentenceEnd >= 0

        def advance(): Unit =
          if (isActive()) {
            indexOfLastSentenceEnd = indexOfNextSentenceEnd
            indexOfNextSentenceEnd =
              text.indexOf("\n\n", indexOfLastSentenceEnd + 1)
          }

        override def hasNext: Boolean =
          isActive()

        override def next(): ConllSent = {

          val sentence = ConllSent(
            tokens = text
              .slice(indexOfLastSentenceEnd, indexOfNextSentenceEnd)
              .trim
              .split("\n")
              .filter {
                _.nonEmpty
              }
              .map {
                parseConllWord
              }
              .toSeq
          )

          advance()

          sentence
        }
      }
    }

  def parseConllWord(line: String): ConllWord = {
    val bits = line.split("\t")
    ConllWord(
      raw = bits(1),
      lemmatized = bits(2),
      posTag = bits(3),
      neTag = bits(4)
    )
  }

  def writeConllSent(s: ConllSent): String =
    s.tokens.zipWithIndex.map {
      case (w, index) => s"$index\t${writeConllWord(w)}"
    }.mkString("\n") + "\n"

  def writeConllWord(w: ConllWord): String =
    s"${w.raw}\t${w.lemmatized}\t${w.posTag}\t${w.neTag}"

}

case class ConllSent(tokens: Seq[ConllWord])

case class ConllWord(
    raw: String,
    lemmatized: String,
    posTag: String,
    neTag: String
)
