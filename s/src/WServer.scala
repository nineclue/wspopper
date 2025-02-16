import cats.effect.IOApp
import cats.effect.IO
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.ember.server.EmberServerBuilder
import fs2.{Stream, Pipe}
import org.http4s.websocket.WebSocketFrame
import org.http4s.dsl.Http4sDsl
import fs2.io.file.Files
import cats.MonadThrow
import com.comcast.ip4s.*
import cats.effect.kernel.Async
import cats.syntax.all.*
import fs2.io.net.Network
import org.http4s.scalatags.*

object WebSocketServer extends IOApp.Simple:
  private val indexHtml =
      import scalatags.Text.all.*
      html(
        head(
            tag("title")("WEBSOCKET"),
            meta(charset := "UTF-8"),
            // meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
            // script(src := "/assets/yader.js"),
            // link(href := "/assets/yader.css", rel := "stylesheet")
        ),
        body(
            div(
              div(id := "output", height := "500px", width := "500px",
                textAlign := "left", overflowY := "scroll", borderRadius := "5px",
                padding := "3px", border := "solid purple 2px"),
              br(),
              form(id := "chatform",
                input(tpe := "text", name := "entry", id := "entry"),
                button(tpe := "submit", "보내기"))
            )
        )
      )

  class Routes[F[_]: Files: MonadThrow] extends Http4sDsl[F]:
    def service (
      wsb: WebSocketBuilder2[F]
    ): HttpApp[F] =
      HttpRoutes.of[F]:
        case request @ GET -> Root / "chat.html" =>
          Ok(indexHtml)
        case GET -> Root / "ws" =>
          val send: Stream[F, WebSocketFrame] = ???
          val receive: Pipe[F, WebSocketFrame, Unit] = ???
          wsb.build(send, receive)
      .orNotFound

  def server[F[_]: Async: Files: Network]: F[Unit] =
    val host = host"0.0.0.0"
    val port = port"8080"
    EmberServerBuilder
      .default[F]
      .withHost(host)
      .withPort(port)
      .withHttpWebSocketApp(wsb => new Routes().service(wsb))
      .build
      .useForever
      .void

  override def run: IO[Unit] = server[IO]
