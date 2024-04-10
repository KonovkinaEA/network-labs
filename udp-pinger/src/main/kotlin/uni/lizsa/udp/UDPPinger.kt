package uni.lizsa.udp

import kotlinx.coroutines.*
import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket

class UDPPinger internal constructor(
    port: Int,
    private val socket: DatagramSocket = DatagramSocket(port)
) : Closeable {

    private val buffer = ByteArray(256)

    private var closed = false
    private lateinit var job: Job

    suspend fun start() = coroutineScope {
        if (closed) return@coroutineScope
        println("The server is running on port ${socket.localPort}.")

        job = launch {
            outer@ while (isActive && !closed) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receiveCancellable(packet) ?: break

                val receivedMsg = String(packet.data, 0, packet.length)
                println("Message received from client ${packet.address.hostAddress}:${packet.port}: $receivedMsg")
                if ((1..10).random() < 4) {
                    println("Drop client packet")
                    continue@outer
                }

                println("Sending response to client ${packet.address.hostAddress}:${packet.port}")
                socket.send(
                    DatagramPacket(
                        receivedMsg.uppercase().toByteArray(),
                        receivedMsg.length,
                        packet.address,
                        packet.port
                    )
                )
            }
        }
    }

    override fun close() {
        closed = true
        runBlocking {
            job.cancel()
            socket.close()
        }
    }
}