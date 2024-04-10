package uni.lizsa.icmp

import java.net.InetAddress
import kotlin.time.Duration
import kotlin.time.measureTimedValue

fun main() {
    val server = "vk.com"

    val address = InetAddress.getByName(server)
    val iterations = 4

    val rtt = mutableListOf<Duration>()
    repeat(iterations) {
        val (success, duration) = measureTimedValue {
            address.isReachable(1000)
        }
        if (success) {
            rtt += duration
            println("Response from ${address.hostAddress}, time = ${duration.inWholeMilliseconds}ms")
        } else {
            println("Request timed out")
        }
    }
    val failures = iterations - rtt.size

    println("\nStatistics:")
    println("\tPackets: Sent = $iterations, Received = ${iterations - failures}, Lost = $failures (${(failures.toDouble() / iterations) * 100}% loss)")
    println("Average round-trip times in milliseconds:")
    val max = if (rtt.isEmpty()) 0 else rtt.max().inWholeMilliseconds
    val min = if (rtt.isEmpty()) 0 else rtt.min().inWholeMilliseconds
    val average = if (rtt.isEmpty()) 0 else rtt.sumOf { it.inWholeMilliseconds } / rtt.size
    println("\tMaximum = ${max}ms, Minimum = ${min}ms, Average = ${average}ms")
}