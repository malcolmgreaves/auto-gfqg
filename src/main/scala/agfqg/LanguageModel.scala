package agfqg

import com.cybozu.labs.langdetect.{Detector, DetectorFactory}

import scala.io.Source

trait LanguageModel {
  def probabilityOf(s: String): Double
}

object LanguageModel {

  lazy val english: LanguageModel = new LanguageModel {

    val isLargeProfile = true

    lazy val toFiName: (String, Boolean) => String =
      (languageCode, isLargeProfile) =>
        s"""/profiles.${if (isLargeProfile) "lg" else "sm"}/$languageCode"""

    lazy val jsonLangProfFromJar: String => String =
      languageCode =>
        Source
          .fromInputStream(
            classOf[DetectorFactory].getResourceAsStream(
              toFiName(languageCode, isLargeProfile))
          )
          .mkString

    lazy val createDetector: Unit => Detector = {

      val javaJsonLanguageProfileList: java.util.List[String] =
        new java.util.ArrayList[String]()
      val _1 = javaJsonLanguageProfileList.add(jsonLangProfFromJar("en"))
      val _2 = javaJsonLanguageProfileList.add(jsonLangProfFromJar("ru")) // need at least 2...

      val x = new DetectorFactory()
      x.loadProfile(javaJsonLanguageProfileList)

      _ =>
        x.create()
    }

    import scala.collection.JavaConverters._

    override def probabilityOf(s: String): Double = {
      val detector = createDetector(())
      detector.append(s)
      detector.getProbabilities.asScala.filter { lang =>
        "en" == lang.lang.toLowerCase
      }.head.prob
    }
  }

}
