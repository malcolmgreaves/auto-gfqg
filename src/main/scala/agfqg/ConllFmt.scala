package agfqg

import java.io.File

import sutils.fp.Types.Err

import scala.io.Source
import scalaz.\/

object ConllFmt {

  def parseConllWord(line: String): ConllWord = {
    val bits = line.split("\t")
    ConllWord(
      raw = bits(1),
      lemmatized = bits(2),
      posTag = bits(3),
      neTag = bits(4)
    )
  }

  /** Produces an Iterator with **MUTABLE INTERNAL STATE** Not threadsafe!. */
  def reader(fi: File): Err[Iterator[ConllSent]] =
    \/.fromTryCatchNonFatal {
      val text = Source.fromFile(fi).mkString
      new Iterator[ConllSent] {

        var indexOfLastSentenceEnd: Int = 0
        var indexOfNextSentenceEnd: Int = text.indexOf("\n\n", 0)
        var active: Boolean = indexOfLastSentenceEnd >= 0

        def advance(): Unit =
          if (active) {
            indexOfLastSentenceEnd = indexOfNextSentenceEnd
            indexOfNextSentenceEnd =
              text.indexOf("\n\n", indexOfLastSentenceEnd + 1)
            active = indexOfLastSentenceEnd >= 0
          }

        override def hasNext: Boolean =
          active && indexOfLastSentenceEnd >= 0

        override def next(): ConllSent = {

          val sentence = ConllSent(
            tokens = text
              .slice(indexOfLastSentenceEnd, indexOfNextSentenceEnd)
              .trim
              .split("\n")
              .filter { _.nonEmpty }
              .map { parseConllWord }
              .toSeq
          )

          advance()

          sentence
        }
      }
    }

  def main(args: Array[String]): Unit = {
    val fi = new File(args.head)
    ConllFmt
      .reader(fi)
      .fold(
        e => throw e,
        iter => {
          val all = iter.toSeq
          System.err.println(s"Found ${all.size} sentences in Conll Formatted file: $fi")
          all.take(10).foreach { x =>
            x.tokens.map { _.raw }.mkString("\n")

            // TODO: change so it outputs the sentence in a single line !

            val s = x.tokens.mkString("\n")
            println(s"\n$s\n")
          }
        }
      )
  }

}

case class ConllWord(
    raw: String,
    lemmatized: String,
    posTag: String,
    neTag: String
)

case class ConllSent(tokens: Seq[ConllWord]) {
  def text: String =
    tokens.map { c =>
      c.raw
    }.mkString(" ")
}
