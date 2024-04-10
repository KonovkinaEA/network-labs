package uni.lizsa.web

import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class WebServer internal constructor(port: Int = 80, opTimeout: Duration = 5000.milliseconds) : Closeable {

    private val socket = ServerSocket(port)

    var closed = false
        private set
    private var connectionsCount = AtomicInteger(0)
    private lateinit var job: Job

    init {
        socket.soTimeout = opTimeout.toInt(DurationUnit.MILLISECONDS)
    }

    suspend fun start() = coroutineScope {
        if (closed) return@coroutineScope

        job = launch {
            outer@ while (isActive) {
                println("Currently awaiting connection on port ${socket.localPort}.")
                val deferredClient = async { socket.acceptCancellable() }

                val clientConnection = deferredClient.await() ?: break@outer
                connectionsCount.incrementAndGet()
                println("Connected to a client at: ${clientConnection.inetAddress}:${clientConnection.port}")
                println("Connected clients: $connectionsCount")
                handleClient(clientConnection)
            }
        }
    }

    private fun CoroutineScope.handleClient(client: Socket) = launch {
        BufferedReader(InputStreamReader(client.inputStream)).use { input ->
            PrintWriter(OutputStreamWriter(client.outputStream)).use { output ->
                try {
                    val path = readFileRequest(client, input)
                    val file = File(path)
                    println("Received request for file $path from ${client.inetAddress.hostAddress}:${client.port}")
                    if (file.exists()) {
                        println("Sending response to ${client.inetAddress.hostAddress}:${client.port}")
                        sendResponse(output, file.readText())
                    } else {
                        println("The file $path requested by ${client.inetAddress.hostAddress}:${client.port} was not found")
                        CustomException("Not Found", 404).sendError(output)
                    }
                } catch (e: CustomException) {
                    println("Encountered an exception while reading request from ${client.inetAddress.hostAddress}:${client.port}: $e")
                    e.sendError(output)
                }
            }
        }

        delay(100)
        println("Closing connection with client ${client.inetAddress}:${client.port}")
        client.close()
        connectionsCount.decrementAndGet()
    }

    private fun CoroutineScope.readFileRequest(client: Socket, istream: BufferedReader): String {
        if (!isActive || client.isClosed) throw CustomException("Connection closed")
        val clientMethodLine = istream.readLine() ?: throw CustomException("No messages have been received.")

        return clientMethodLine.parseMethodLine()
    }

    override fun close() {
        closed = true
        runBlocking {
            job.cancel()
            socket.close()
        }
    }

    fun stop() = close()

    private fun sendResponse(out: PrintWriter, content: String) {
        out.println(
            """
HTTP/1.1 200 OK

$content
            """.trimIndent()
        )
    }
}
