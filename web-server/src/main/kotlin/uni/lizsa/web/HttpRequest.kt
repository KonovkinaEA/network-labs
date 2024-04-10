package uni.lizsa.web

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import java.io.*
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class HttpRequest internal constructor(
    server: String,
    port: Int,
    opTimeout: Duration = 1000.milliseconds
) : Closeable {

    private val socket = Socket(server, port)

    init {
        socket.soTimeout = opTimeout.toInt(DurationUnit.MILLISECONDS)
    }

    override fun close() {
        socket.close()
    }

    suspend fun requestFile(filepath: String) = coroutineScope {
        if (socket.isClosed) return@coroutineScope
        BufferedReader(InputStreamReader(socket.inputStream)).use { input ->
            PrintWriter(OutputStreamWriter(socket.outputStream), true).use { out ->
                out.println(buildRequest(socket.inetAddress.hostAddress, filepath))

                val (code, message) = input.readLine().getResponseCode()
                if (code != 200) {
                    println("The server has responded with error code $code and message: $message")
                } else {
                    println("Received successful response from the server")

                    do {
                        val line = input.readLine() ?: break
                    } while (line.isNotBlank())

                    println("The server has sent the content of the file $filepath:")
                    while (isActive && !socket.isClosed) {
                        val fileLine = input.readLine() ?: break
                        println(fileLine)
                    }
                }

                if (!socket.isClosed) socket.close()
                println("The client has closed the connection with the server")
            }
        }
    }
}
