package uni.lizsa.udp

import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class UDPClient internal constructor(private val socket: DatagramSocket = DatagramSocket()) : Closeable {

    private val buffer = ByteArray(256)

    private var counter = -1

    fun ping(server: String, port: Int, timeout: Duration = 1000.milliseconds): Duration {
        counter++
        socket.soTimeout = timeout.inWholeMilliseconds.toInt()

        val before = LocalDateTime.now()
        val message =
            "Pinging $server: try $counter at ${before.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}".toByteArray()
        val serverAddress = InetAddress.getByName(server)
        val packet = DatagramPacket(message, message.size, serverAddress, port)
        println("Pinging the server at ${serverAddress.hostAddress}:$port")
        socket.send(packet)

        val receivedPacked = DatagramPacket(buffer, buffer.size)
        try {
            socket.receive(receivedPacked)
        } catch (e: SocketTimeoutException) {
            println("Timeout for request $counter")
            throw e
        }
        val receivesMessage = String(receivedPacked.data, 0, receivedPacked.length)
        val rtt = (LocalDateTime.now()
            .getLong(ChronoField.MILLI_OF_SECOND) - before.getLong(ChronoField.MILLI_OF_SECOND)).milliseconds
        println("$counter) Message: $receivesMessage, RTD: ${rtt.inWholeMilliseconds} ms")

        return rtt
    }

    override fun close() {
        socket.close()
    }
}