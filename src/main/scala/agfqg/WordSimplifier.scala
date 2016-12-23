package agfqg

import edu.stanford.nlp.process.Stemmer

trait WordSimplifier {
  def simplify(x: String): String
}

object WordSimplifier {

  lazy val noop: WordSimplifier = new WordSimplifier {
    override def simplify(x: String) = x
  }

  /** Mutable stemmer !!!!!! Assumes input is lower-cased */
  lazy val mut_porterStemmerCoreNlp: WordSimplifier = new WordSimplifier {
    val s = new Stemmer()
    override def simplify(x: String) =
      s.stem(x)
  }

  lazy val porterStemmerCoreNlp: WordSimplifier = new WordSimplifier {
    override def simplify(x: String) =
      new Stemmer().stem(x.toLowerCase)
  }

}
