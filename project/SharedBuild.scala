import sbt._
import Keys._

object SharedBuild {

  // // // // // // // // // //
  // //     Versions      // //
  // // // // // // // // // //

  lazy val fp4mlV      = "0.0.0"
  lazy val dataTcV     = "0.0.0"
  lazy val sutilV      = "0.3.0"
  lazy val scalaMacroV = "2.1.0"
  lazy val silencerV   = "0.5"
  lazy val sistaV      = "6.0.1"
  lazy val coreNlpV    = "3.6.0"
  lazy val raptureV    = "2.0.0-M7"
  lazy val langDtV     = "1.1.1"

  // // // // // // // // // //
  // //    Dependencies   // //
  // // // // // // // // // //

  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % Test
  )

  lazy val mainDeps = Seq( 
   "io.malcolmgreaves" %% "s-util-fp" % sutilV,
   "org.clulab" % "processors-main_2.11"    % sistaV ,
   "org.clulab" % "processors-models_2.11"  % sistaV,
   "org.clulab" % "processors-corenlp_2.11" % sistaV,
   "edu.stanford.nlp" % "stanford-corenlp" % coreNlpV,
   "edu.stanford.nlp" % "stanford-parser"  % coreNlpV,
   "io.malcolmgreaves" %% "cybozu-language-detection" % langDtV,
   "com.propensive"         %% "rapture"       % raptureV,
    // [BEGIN] PUBLISHED LOCAL
    "io.malcolmgreaves" %% "fp4ml-spark" % fp4mlV,
    // ^^ includes all of below:
    // "io.malcolmgreaves" %% "fp4ml-main" % fp4mlV,
    // "io.malcolmgreaves" %% "data-tc-spark" % dataTcV,
    // "io.malcolmgreaves" %% "data-tc-extra" % dataTcV
    // [END]   PUBLISHED LOCAL
   "com.github.ghik" %% "silencer-lib" % silencerV
  )

  // // // // // // // // // //
  // //      Plugins      // //
  // // // // // // // // // //

  lazy val scalaMacros =
    "org.scalamacros" % "paradise" % scalaMacroV cross CrossVersion.full

  lazy val warningSilencer = 
    "com.github.ghik" %% "silencer-plugin" % silencerV
 
}
