
lazy val root = (project in file(".")).settings(
      name := "json-spec",
      scalaVersion := "2.11.7",
      resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "ammonite-ops" % "0.5.2"
      )
    )
