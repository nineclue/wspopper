import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

object Messages:
    enum Message:
        case Hello, Bye
        case Open(url: String)
        case Notify(msg: String)
        case Age(age: Int)
        case Read(mAge: Age)
        // case Read(m: Message)
        case Ping, Pong

    given messageCodec: JsonValueCodec[Message] = JsonCodecMaker.make
