package uni.lizsa.proxy

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

fun main() {
    val remoteServer = "google.com"
    val remotePort = 80
    val port = 36841

    val proxy = ProxyServer(remoteServer, remotePort, port)

    runBlocking {
        launch {
            withContext(Dispatchers.IO) {
                proxy.start()
            }
        }
        launch {
            delay(100)

            println("GET request of /index.html page")
            val socket = Socket("127.0.0.1", port)
            val writer = PrintWriter(OutputStreamWriter(socket.outputStream), true)
            val reader = BufferedReader(InputStreamReader(socket.inputStream))
            writer.println("GET /index.html")

            println("Got answer from the server:")
            reader.readLines().joinToString("\n").also { println(it) }
        }
    }
}