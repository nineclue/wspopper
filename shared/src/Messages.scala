import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

object Messages:
    enum Message:
        case Hello, Bye
        case Open(url: String)
        case Age(age: Int)
        case Read(m: Age)
        case Ping, Pong

    given messageCodec: JsonValueCodec[Message] = JsonCodecMaker.make
