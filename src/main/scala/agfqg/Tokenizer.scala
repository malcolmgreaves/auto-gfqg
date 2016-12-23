package agfqg

import java.io.StringReader

import edu.stanford.nlp.process.PTBTokenizer

trait Tokenizer {
  def tokenize(s: String): Seq[String]
  def tokenizeWithStarts(s: String): Seq[(String, Int)]
}

object Tokenizer {

  lazy val ptbTokenizer: Tokenizer = new Tokenizer {

    import scala.collection.JavaConverters._

    override def tokenize(s: String) =
      PTBTokenizer
        .newPTBTokenizer(new StringReader(s))
        .tokenize()
        .asScala
        .map { _.word() }

    override def tokenizeWithStarts(s: String) =
      PTBTokenizer
        .newPTBTokenizer(new StringReader(s))
        .tokenize()
        .asScala
        .map { word =>
          (word.word(), word.beginPosition())
        }
  }

}
