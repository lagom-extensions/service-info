name := "lagom-service-info"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.gu"                     %% "scanamo-alpakka" % "1.0.0-M8" % "provided",
  "com.lightbend.akka"         %% "akka-stream-alpakka-kinesis" % "1.0-M1" % "provided",
  "org.typelevel"              %% "cats-effect" % "1.1.0",
  "org.springframework.boot"   % "spring-boot-starter-actuator" % "2.1.1.RELEASE",
  "org.springframework.boot"   % "spring-boot-starter-jdbc" % "2.1.1.RELEASE",
  lagomScaladslApi             % "provided",
  lagomScaladslPersistenceJdbc % "provided"
)

libraryDependencies ++= Seq(compilerPlugin("com.github.ghik" %% "silencer-plugin" % "1.3.0"), "com.github.ghik" %% "silencer-lib" % "1.3.0" % Provided)
scalacOptions += "-P:silencer:pathFilters=target/.*"
scalacOptions += s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"

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
