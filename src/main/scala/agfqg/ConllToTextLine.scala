package agfqg

import java.io.File

object ConllToTextLine {

  def main(args: Array[String]): Unit = {

    val fi = new File(args.head)
    ConllFmt
      .reader(fi)
      .fold(
        e => throw e,
        iter => {
          val all = iter.toSeq
          System.err.println(
            s"Found ${all.size} sentences in Conll Formatted file: $fi")

          all.take(10).foreach { x =>
            x.tokens.map {
              _.raw
            }.mkString("\n")

            // TODO: change so it outputs the sentence in a single line !

            val s = x.tokens.mkString("\n")
            println(s"\n$s\n")
          }
        }
      )
  }

}
