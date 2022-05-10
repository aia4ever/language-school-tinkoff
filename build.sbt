import scala.language.postfixOps

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val http4sVersion = "0.23.11"

lazy val doobieVersion = "1.0.0-RC2"

resolvers += Resolver.sonatypeRepo("snapshots")


lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres-circe" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion % "test",
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",
)

lazy val http4s = Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
)


libraryDependencies ++= Seq(

  "io.circe"        %% "circe-generic" % "0.14.1",

  "org.postgresql" % "postgresql" % "42.3.4",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

  "org.slf4j" % "slf4j-api" % "1.7.36",

  "ch.qos.logback" % "logback-classic" % "1.2.11",

  "com.github.pureconfig" %% "pureconfig" % "0.14.0",

  "org.typelevel" %% "cats-core" % "2.7.0",

  "org.typelevel" %% "cats-effect" % "3.3.5",

  "org.scalatest" %% "scalatest" % "3.2.11" % "test"
) ++ doobie ++ http4s

enablePlugins(FlywayPlugin)
version := "0.0.1"
name := "flyway-sbt-test1"

libraryDependencies += "org.hsqldb" % "hsqldb" % "2.5.0"

flywayUrl := "jdbc:postgresql://localhost:5432/postgres"
flywayUser := "aia4ever"
flywayPassword := "password"
flywayLocations += ("db/migration")

lazy val root = (project in file("."))
  .settings(
    name := "coursework"
  )


