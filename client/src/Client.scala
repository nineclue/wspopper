import Messages.Message, Message.* 
import org.scalajs.dom, dom.{WebSocket, document, MessageEvent, HTMLInputElement}
import scalatags.JsDom.all.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import org.scalajs.dom.HTMLInputElement

object Client:
    // import scala.scalajs.js, js.annotation.*
    // @js.native
    // @JSImport("ws", "WebSocket")
    // class WebSocket(host: String) extends js.Object:

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

    def main(as: Array[String]): Unit = 
        setupWebSocket

    def setupWebSocket = 
        ws = WebSocket(wsHost)
        ws.onmessage = websocketMessage _
        ws.onclose = _ => { println("Websocket closed!!!") }
        ws.onerror = e => { println(s"Error occured! : $e") }
        document.getElementById("sendAge").addEventListener("click", _ => { 
            val age = document.getElementById("age").asInstanceOf[HTMLInputElement].value
            println(s"Age string : '$age'")
            val message = writeToString(Age(age.toInt))
            println(message)
            ws.send(message)
        })
        document.getElementById("bye").addEventListener("click", _ => { 
            val message = writeToString(Bye)
            println(message)
            ws.send(message) 
        })
        dom.window.setTimeout(() => { println("sending refresh"); ws.send(writeToString(Ping)) }, 300)
