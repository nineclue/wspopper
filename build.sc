import mill._, scalalib._, scalajslib._, scalajslib.api._

trait AppScalaModule extends ScalaModule {
  def scalaVersion = "3.3.5"
  def scalaTagsVersion = "0.13.1"
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSModule {
  def scalaJSVersion = "1.18.2"
}

object `package` extends RootModule with AppScalaModule {
  val Http4sVersion = "0.23.30"
  def moduleDeps = Seq(shared.jvm)
  def ivyDeps = Agg(
      ivy"org.http4s::http4s-ember-server:$Http4sVersion",
      ivy"org.http4s::http4s-dsl:$Http4sVersion",
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
        ivy"org.http4s::http4s-scalatags::0.25.2",
      )
    }

    object jvm extends SharedModule
    object js extends SharedModule with AppScalaJSModule
  }

  object client extends AppScalaJSModule {
    def defaultCommandName() = "fastLinkJS"
    def moduleDeps = Seq(shared.js)
    def ivyDeps = Agg(
      ivy"org.scala-js::scalajs-dom::2.8.0"
    )
	  def moduleKind = ModuleKind.ESModule
  }
}
