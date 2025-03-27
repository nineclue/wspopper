import mill._, scalalib._, scalajslib._, scalajslib.api._
import $ivy.`io.github.nafg.millbundler::millbundler::0.2.0`
import io.github.nafg.millbundler.jsdeps._
import io.github.nafg.millbundler._

trait AppScalaModule extends ScalaModule {
  def scalaVersion = "3.3.5"
  def scalaTagsVersion = "0.13.1"
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSRollupModule {
  def scalaJSVersion = "1.18.2"
  def fs2Version = "3.12.0"
}

object `package` extends RootModule with AppScalaModule {
  val Http4sVersion = "0.23.30"
  def sources = Task.Sources { "server" }
  def moduleDeps = Seq(shared.jvm)
  def ivyDeps = Agg(
      ivy"org.http4s::http4s-ember-server:$Http4sVersion",
      ivy"org.http4s::http4s-dsl:$Http4sVersion",
      ivy"org.http4s::http4s-scalatags::0.25.2",
      ivy"com.lihaoyi::scalatags::$scalaTagsVersion",
  )
  /*
  def resources = Task {
    os.makeDir(Task.dest / "webapp")
    val jsPath = client.fastLinkJS().dest.path
    os.copy(jsPath / "main.js", Task.dest / "webapp/main.js")
    os.copy(jsPath / "main.js.map", Task.dest / "webapp/main.js.map")
    super.resources() ++ Seq(PathRef(Task.dest))
  }
  */

  /*
  object test extends ScalaTests with TestModule.Utest {

    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest::0.8.5",
      ivy"com.lihaoyi::requests::0.6.9"
    )
  }
  */

  object shared extends Module {
    trait SharedModule extends AppScalaModule with PlatformScalaModule {
      val JsonIterVersion = "2.33.3"
      def compileIvyDeps = Agg(
        ivy"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros::$JsonIterVersion",
      )
      def ivyDeps = Agg(
    		ivy"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core::$JsonIterVersion",
      )
    }

    object jvm extends SharedModule
    object js extends SharedModule with AppScalaJSModule
  }

  object client extends AppScalaJSModule {
    def defaultCommandName() = "fastBundle"
    def moduleDeps = Seq(shared.js)
    def ivyDeps = Agg(
      ivy"org.scala-js::scalajs-dom::2.8.0",
      ivy"com.lihaoyi::scalatags::$scalaTagsVersion",
      ivy"co.fs2::fs2-core::$fs2Version",
      ivy"org.scalablytyped::tauri-apps__api::2.4.0-75057a",
      ivy"org.scalablytyped::tauri-apps__plugin-log::2.3.1-3af46a",
      ivy"org.scalablytyped::tauri-apps__plugin-notification::2.2.2-bd56a8",
    )
    def jsDeps = 
      super.jsDeps() ++
        JsDeps(
          dependencies = Map(
            "@tauri-apps/api" -> "^2.4.0",
            "@tauri-apps/plugin-log" -> "~2",
            "@tauri-apps/plugin-notification" -> "~2"
          )          
        )
	  def moduleKind = ModuleKind.ESModule
    def fastBundle = Task {
      val jsPath = devBundle().head.path / os.up
      val targetPath = jsPath / os.up / os.up / os.up / "src"
      val target = targetPath / "main.js"
      os.copy.over(jsPath / "out-bundle.js", target)
      val sourceMap = jsPath / "out-bundle.js.map"
      if (os.exists(sourceMap)) {
          os.copy.over(sourceMap, targetPath / "main.js.map")
      }
      PathRef(target)
    }
    def fullBundle = Task {
      val jsPath = prodBundle().head.path / os.up
      val targetPath = jsPath / os.up / os.up / os.up / "src" // root src
      val target = targetPath / "main.js"
      os.copy.over(jsPath / "out-bundle.js", target)
      val sourceMap = jsPath / "out-bundle.js.map"
      if (os.exists(sourceMap)) {
          os.copy.over(sourceMap, targetPath / "main.js.map")
      }
    }
  }
}
