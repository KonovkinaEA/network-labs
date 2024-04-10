package uni.lizsa.web

import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val PORT = 8080
private const val DURATION = 10000L

private val paths = listOf(
    "web-server/src/main/resources/text.txt",
    "web-server/src/main/resources/markup.html",
    "web-server/src/main/resources/notexist.txt"
)

fun main(): Unit = runBlocking {
    launch {
        var serverRunning = { false }

        launch {
            val server = runServer(PORT, DURATION.milliseconds)
            serverRunning = { !server.closed }
        }
        launch {
            while (!serverRunning()) {
                delay(50)
            }

            while (serverRunning()) {
                launch {
                    try {
                        val client = HttpRequest("127.0.0.1", PORT)
                        delay((500L..2000L).random())
                        client.requestFile(paths.random())
                    } catch (_: Exception) {
                    }
                }
                delay((200L..2000L).random())
            }
        }
    }
}

fun CoroutineScope.runServer(port: Int, terminationDelay: Duration): WebServer {
    val server = WebServer(port = port)
    launch {
        withContext(Dispatchers.IO) {
            server.start()
        }
    }
    launch {
        delay(terminationDelay)
        server.stop()
    }

    return server
}
