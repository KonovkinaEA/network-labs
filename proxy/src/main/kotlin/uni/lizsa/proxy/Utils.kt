package uni.lizsa.proxy

internal fun createFileResponse(content: String) = """
HTTP/1.1 200 OK

$content
""".trimIndent()
