name := "lagom-service-info"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  lagomScaladslApi % "provided"
)

organization := "com.github.lagom-extensions"
homepage := Some(url("https://github.com/lagom-extensions/service-info"))
scmInfo := Some(ScmInfo(url("https://github.com/lagom-extensions/service-info"), "git@github.com:lagom-extensions/service-info.git"))
developers := List(Developer("kuzkdmy", "Dmitriy Kuzkin", "mail@dmitriy.kuzkin@gmail.com", url("https://github.com/kuzkdmy")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
  else Opts.resolver.sonatypeStaging
)
