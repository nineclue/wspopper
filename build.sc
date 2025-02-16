import mill._, scalalib._

object s extends ScalaModule {
  val Http4sVersion = "0.23.30"
  def scalaVersion = "3.6.3"
  def ivyDeps = Agg(
      ivy"org.http4s::http4s-ember-server:$Http4sVersion",
      ivy"org.http4s::http4s-dsl:$Http4sVersion",
      ivy"org.http4s::http4s-scalatags:0.25.2",
      ivy"com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-core:2.33.2",
    )
}
