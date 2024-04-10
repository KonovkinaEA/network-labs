package uni.lizsa.proxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class ProxyServer(private val server: String, remotePort: Int, port: Int = remotePort) : Closeable {

    private val serverSocket = ServerSocket(port)
    private val socket = Socket(server, remotePort)

    init {
        require(remotePort >= 0)
        require(port >= 0)
    }

    suspend fun start() = coroutineScope {
        if (socket.isClosed) return@coroutineScope

        println("Awaiting connection on port ${serverSocket.localPort}")
        val client = serverSocket.accept()
        println("Connected to a client at: ${client.inetAddress}:${client.port}")

        launch(Dispatchers.IO) {
            BufferedReader(InputStreamReader(client.inputStream)).use { reader ->
                PrintWriter(OutputStreamWriter(client.outputStream), true).use { writer ->
                    BufferedReader(InputStreamReader(socket.inputStream)).use { remoteReader ->
                        PrintWriter(OutputStreamWriter(socket.outputStream), true).use { remoteWriter ->
                            val clientRequest = reader.readLine() ?: return@launch
                            println("Received request from the client: $clientRequest")
                            val dir = File(
                                javaClass.classLoader.getResource("lighthouse")?.toURI() ?: return@launch
                            ).parentFile
                            val file = File(dir, clientRequest.split("\\s+".toRegex())[1])
                            if (file.isFile) {
                                println("The requested file $file is present in the cache.")
                                writer.println(createFileResponse(file.readText()))
                            } else {
                                println("The requested file $file is not present in the cache.")
                                println("Retrieve it from server $server:${socket.port}")

                                remoteWriter.println(clientRequest)
                                val content = remoteReader.readLines().dropWhile { it.isNotBlank() }.joinToString("\n")
                                file.writeText(content)
                                writer.println(createFileResponse(content))
                                println("The file $file has been cached.")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun close() {
        if (!socket.isClosed) socket.close()
        if (!serverSocket.isClosed) serverSocket.close()
    }
}