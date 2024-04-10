package uni.lizsa.smtp

import java.io.Closeable
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class SmtpClient : Closeable {

    open val port: Int = 25

    protected lateinit var socket: Socket
    var connected = false
        protected set

    open fun connect(smtpServer: String, timeout: Duration = 10.seconds) {
        connected = true
        socket = Socket(smtpServer, port)
        socket.soTimeout = timeout.inWholeMilliseconds.toInt()
        socket.expectedResponse(220)
    }

    open fun <T> send(buildMessage: Message.() -> T) {
        require(connected) { "Missed connection" }
        with(socket) {
            val message = Message().apply { buildMessage() }
            expectedResponse(250, "HELLO Alice")
            expectedResponse(250, "MAIL FROM: <${message.addressFrom}>")
            expectedResponse(250, "RCPT TO: <${message.addressTo}>")
            expectedResponse(354, "DATA")
            expectedResponse(250, message.content)
            expectedResponse(221, "QUIT")
        }
    }

    override fun close() {
        if (connected) {
            println("Connection closed")
            socket.close()
        }
    }
}
