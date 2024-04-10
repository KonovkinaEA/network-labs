package uni.lizsa.web

import java.io.PrintWriter

class CustomException(message: String, val code: Int? = null) : Exception(message) {
    override fun toString(): String = "Exception(message: ${message}, code = $code)"

    fun sendError(out: PrintWriter) {
        val code = code ?: INTERNAL_SERVER_ERROR_CODE
        out.println(
            """
HTTP/1.1 $code $message
            """.trimIndent()
        )
    }

    companion object {

        private const val INTERNAL_SERVER_ERROR_CODE = 500
    }
}
