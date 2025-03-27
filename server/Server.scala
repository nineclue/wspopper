import cats.effect.*
import org.http4s.{HttpApp, HttpRoutes, StaticFile}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.ember.server.EmberServerBuilder
import fs2.{Stream, Pipe}
import cats.effect.std.{Queue, AtomicCell}
import fs2.concurrent.Topic
import org.http4s.websocket.WebSocketFrame
import org.http4s.dsl.Http4sDsl
import com.comcast.ip4s.*
import cats.syntax.all.*
import scala.concurrent.duration.*
import org.http4s.scalatags.*
import scalatags.Text.all.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import Messages.*, Message.*

object Server extends IOApp:
    private val indexHtml =
        html(
            head(
                tag("title")("WebSocket Test"),
                meta(charset := "UTF-8"),
                script(src := "main.js", `type` := "module", defer)
            ),
            body(
                h1("WebSocket Messages"),
                div(input(id := "age"), div(id := "sendAge", "Send")),
                div(id := "bye", "Bye"),
                div(id := "messages", display := "flex", flexDirection := "column"),
            )
        )

    class Routes extends Http4sDsl[IO]:
        def service (
            wsb: WebSocketBuilder2[IO],
            queue: Queue[IO, WebSocketFrame],
            topic: Topic[IO, WebSocketFrame], 
        ): HttpApp[IO] =
            HttpRoutes.of[IO]:
                case request @ GET -> Root =>
                    Ok(indexHtml)
                case request@GET -> Root / "main.js" =>
                    StaticFile.fromPath(fs2.io.file.Path("./out/client/fastLinkJS.dest/main.js"), Some(request)).getOrElseF(NotFound())
                case GET -> Root / "ws" =>
                    val send: Stream[IO, WebSocketFrame] = 
                        topic.subscribe(maxQueued = 1000)
                        // Stream.awakeEvery[IO](1.minute).evalMap(t => IO(WebSocketFrame.Text(s"time : $t passed")))
                    val receive: Pipe[IO, WebSocketFrame, Unit] = 
                        _.flatMap: wsf =>
                            val command = readFromString[Message](String(wsf.data.toArray))
                            command match
                                case Ping => 
                                    println("Pong")
                                    // refreshListAsJson(plist).map(WebSocketFrame.Text(_)).foreach(queue.offer)
                                    Stream.eval(queue.offer(WebSocketFrame.Text(writeToString(Pong))))
                                case Age(age) =>
                                    Stream.eval(queue.offer(WebSocketFrame.Text(writeToString(Read(Age(age))))))
                                case Bye => 
                                    Stream.eval(IO.println("Let's byebye") >> queue.offer(WebSocketFrame.Close()))
                                case _ =>
                                    Stream.eval(IO.println(s"Got data $command"))
                    wsb.build(send, receive)
            .orNotFound

    def server(queue: Queue[IO, WebSocketFrame], topic: Topic[IO, WebSocketFrame]): IO[Unit] =
        val host = host"0.0.0.0"
        val port = port"8080"
        EmberServerBuilder
            .default[IO]
            .withHost(host)
            .withPort(port)
            .withHttpWebSocketApp(wsb => new Routes().service(wsb, queue, topic))
            .build
            .useForever
            .void

    val p =
        for
            q       <- Queue.unbounded[IO, WebSocketFrame]
            t       <- Topic[IO, WebSocketFrame]
            s       <- Stream(
                        Stream.fromQueueUnterminated(q).through(t.publish),
                        Stream.awakeEvery[IO](30.seconds)
                            .map(_ => WebSocketFrame.Ping())
                            .through(t.publish),
                        Stream.eval(server(q, t)),
                        ).parJoinUnbounded.compile.drain
        yield s

    override def run(as: List[String]): IO[ExitCode] =
        p.as(ExitCode.Success)
