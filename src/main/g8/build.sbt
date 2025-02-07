val scala213 = "2.13.5"
val scala212 = "2.12.13"
val scala211 = "2.11.12"
val dottyNext = "3.0.0-RC2"
val scalaJSVersion = "1.5.1"
val scalaNativeVersion = "0.4.0"
val mUnitVersion = "0.7.23"

val scala2Versions = List(scala213, scala212, scala211)
val scala3Versions = List(dottyNext)
val allScalaVersions = scala2Versions ++ scala3Versions

inThisBuild(
  List(
    scalaVersion := scala213,
    organization := "$package$",
    homepage := Some(url("$homepage$")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "$githubUserNoSpaceLowercase$",
        "$githubUser$",
        "$githubEmail$",
        url("$githubUserUrl$")
      )
    ),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val sharedSettings = Seq(
  name := "$libraryNameHyphen$",
  scalaVersion := scala213,
  excludeFilter in (Compile, unmanagedResources) := NothingFilter,
  scalafmtOnCompile in Compile := true,
  scalafmtOnCompile in Test := true,
  testFrameworks += new TestFramework("munit.Framework"),
  logBuffered := false,
  scalacOptions in (Compile, doc) += "-groups",
  scalacOptions += "-Ywarn-unused", // required by `RemoveUnused` rule
  parallelExecution in Test := false,
  libraryDependencies += "org.scalameta" %%% "munit" % mUnitVersion % Test,
  headerLicense := Some(HeaderLicense.ALv2("2020", "$githubUser$"))
)

skip in publish := true
crossScalaVersions := List()
libraryDependencies += "org.scalameta" %%% "munit" % mUnitVersion % Test

lazy val jVMSettings = List(
  crossScalaVersions := allScalaVersions,
  gitHubPagesOrgName := "$githubUserNoSpaceLowercase$",
  gitHubPagesRepoName := "$libraryNameHyphen$",
  gitHubPagesSiteDir := baseDirectory.value / "target" / "site"
)

lazy val jSSettings = List(
  crossScalaVersions := allScalaVersions,
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  libraryDependencies ++= List(
    ("org.scala-js" %% "scalajs-test-interface" % scalaJSVersion % Test)
      .withDottyCompat(scalaVersion.value),
    ("org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion % Test)
      .withDottyCompat(scalaVersion.value)
  )
)

lazy val nativeSettings = List(
  scalaVersion := scala213,
  crossScalaVersions := scala2Versions,
  libraryDependencies ++= List(
    "org.scala-native" %%% "test-interface" % scalaNativeVersion % Test,
    "org.scala-native" %%% "junit-runtime"  % nativeVersion      % Test
  )
)

lazy val root = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(sharedSettings)
  .jvmSettings(jVMSettings)
  .jsSettings(jSSettings)
  .nativeSettings(nativeSettings)
  .jvmConfigure(
    _.enablePlugins(AutomateHeaderPlugin, GitHubPagesPlugin, SiteScaladocPlugin)
  )

lazy val rootJVM = root.jvm
lazy val rootJS = root.js
lazy val rootNative = root.native

lazy val docs = project
  .in(file("project-mdoc"))
  .dependsOn(rootJVM)
  .settings(
    sharedSettings,
    mdocIn := baseDirectory.in(rootJVM).value / ".." / "src" / "docs",
    mdocOut := baseDirectory.in(rootJVM).value / "..",
    mdocVariables := Map(
      "VERSION"                  -> previousStableVersion.value.getOrElse("0.1.0"),
      "SCALA_NATIVE_VERSION"     -> scalaNativeVersion,
      "SCALA_JS_VERSION"         -> scalaJSVersion,
      "DOTTY_NEXT_VERSION"       -> dottyNext,
      "SUPPORTED_SCALA_VERSIONS" -> allScalaVersions.map(v => s"`\$v`").mkString(", ")
    ),
    skip in publish := true
  )
  .enablePlugins(MdocPlugin)

addCompilerPlugin("org.scala-native" % "junit-plugin" % nativeVersion cross CrossVersion.full)
