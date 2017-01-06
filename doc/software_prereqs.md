# Software Pre-Requisites

Ensure that you have the following on your machine:
* `java` version 1.8
* `sbt` version 0.13.11
* `gcc` version 5.4.0
* `make` version 4.1
* The following text within `$HOME/.sbt/0.13/plugins/plugins.sbt`: `addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.7.4")`. This the `sbt-pack` plugin, which is necessary for creating executables of the project's Scala code.

Additionally, when running the many scripts within this project, set the `DEV` environment variable to be the directory where you will checkout all code. I.e., when checked out on your machine, this repostiory will be at `$DEV/auto-gfqg`.
