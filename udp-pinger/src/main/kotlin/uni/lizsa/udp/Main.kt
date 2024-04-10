package uni.lizsa.udp

import kotlinx.coroutines.*
import java.net.SocketTimeoutException
import kotlin.time.Duration

fun main(): Unit = runBlocking {
    val port = 1234
    val pingIterations = 10
    val server = UDPPinger(port)
    val client = UDPClient()

    launch {
        launch {
            withContext(Dispatchers.IO) {
                server.start()
            }
        }

        launch {
            delay(100)
            var failures = 0
            val rtt = ArrayList<Duration>(pingIterations)
            repeat(pingIterations) {
                try {
                    rtt += client.ping("127.0.0.1", port)
                } catch (_: SocketTimeoutException) {
                    failures++
                }
            }
            client.close()
            server.close()

            println()
            println("Packets: Sent = $pingIterations, Received = ${pingIterations - failures}, Lost = $failures (${(failures.toDouble() / pingIterations) * 100}% loss)")
            println("Approximate round trip times in milli-seconds:")
            println("Minimum = ${rtt.min().inWholeMilliseconds}ms, Maximum = ${rtt.max().inWholeMilliseconds}ms, Average = ${rtt.sumOf { it.inWholeMilliseconds } / rtt.size}ms")
        }
    }
}