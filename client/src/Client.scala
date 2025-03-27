import Messages.Message, Message.* 
import org.scalajs.dom, dom.{WebSocket, document, MessageEvent, HTMLInputElement}
import scalatags.JsDom.all.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import org.scalajs.dom.HTMLInputElement
import typings.tauriAppsApi.trayMod
import typings.tauriAppsPluginLog.{mod => logMod}, logMod.{debug, error, info, trace, warn}
import typings.tauriAppsPluginNotification.{mod => notiMod}
import cats.effect.*
import fs2.{Stream}
import scala.concurrent.duration.*
import cats.syntax.all.*

object Client extends IOApp:
    extension[A] (p: scalajs.js.Promise[A])
        def toIOEither: IO[Either[String, A]] = 
            val success = (v: A) => v.asRight[String]
            val error = (e: Any) => e.asInstanceOf[String].asLeft[A]
            IO.fromFuture(IO(p.`then`(success, error).toFuture))
        def toIO: IO[A] = 
            IO.fromFuture(IO(p.`then`(v => v, e => error(e.asInstanceOf[String]).asInstanceOf[A]).toFuture))

    private var ws: WebSocket = scala.compiletime.uninitialized
    inline private val wsHost = "ws://192.168.50.212:8080/ws"

    def websocketMessage(e: MessageEvent) =
        val m = readFromString[Message](e.data.asInstanceOf[String])
        println(m)
        val divText = m match
            case Hello => "Hello"
            case Open(url) => s"Let's open $url"
            case Age(age) => s"Age is $age"
            case Read(Age(age)) => s"Server confirmed Age($age)"
            case _ => "Unknown message"
        document.getElementById("messages").appendChild(div(divText).render)

    def run(args: List[String]): IO[ExitCode] = 
        for 
            _   <- IO(trace("시작합니다!"))
            _   <- setupWebSocket
        yield ExitCode.Success

    def setupWebSocket =
        IO:
            ws = WebSocket(wsHost)
            ws.onmessage = websocketMessage _
            ws.onclose = _ => { info("Websocket closed!!!") }
            ws.onerror = e => { error(s"Error occured! : $e") }
            document.getElementById("sendAge").addEventListener("click", _ => { 
                val age = document.getElementById("age").asInstanceOf[HTMLInputElement].value
                debug(s"Age string : '$age'")
                val message = writeToString(Age(age.toInt))
                debug(message)
                ws.send(message)
            })
            document.getElementById("bye").addEventListener("click", _ => { 
                val message = writeToString(Bye)
                println(message)
                ws.send(message) 
            })
        >>
            (IO.sleep(300.millis) >> IO({ trace("SetupWebSockt:sending refresh"); ws.send(writeToString(Ping))}))
