package uni.lizsa.web

import java.io.IOException
import java.net.ServerSocket

internal fun buildRequest(host: String, filepath: String) = """
GET $filepath HTTP/1.1
Host: $host
Connection: Keep-Alive
        """.trimIndent()

internal fun String.getResponseCode(): Pair<Int, String> {
    val strs = split("\\s+".toRegex())
    return Pair(strs[1].toInt(), strs.subList(2, strs.size).joinToString(" "))
}

internal fun String.parseMethodLine(): String {
    val trimmed = trimIndent()
    if (trimmed.isEmpty()) {
        throw CustomException("Invalid empty request")
    }

    val split = trimmed.split("""\s+""".toRegex())
    if (!split.first().equals("GET", ignoreCase = true)) {
        throw CustomException("Unknown method: ${split.first()}")
    }

    if (!split.last().startsWith("HTTP/", ignoreCase = true)) {
        throw CustomException("Unknown scheme: ${split.last()}")
    }

    if (split.last().substringAfter("HTTP/") != "1.1") {
        throw CustomException("Unknown HTTP version: ${split.last().substringAfter("HTTP/")}")
    }

    return split.subList(1, split.lastIndex).joinToString(" ")
}

internal fun ServerSocket.acceptCancellable() = try {
    accept()
} catch (e: IOException) {
    null
}
