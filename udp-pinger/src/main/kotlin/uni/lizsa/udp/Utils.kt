package uni.lizsa.udp

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket

internal fun DatagramSocket.receiveCancellable(packet: DatagramPacket) = try {
    receive(packet)
} catch (e: IOException) {
    null
}
