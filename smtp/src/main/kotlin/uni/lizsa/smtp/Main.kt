package uni.lizsa.smtp

private val arguments = arrayOf("smtp.gmail.com", "mail1@gmail.com", "mail2@gmail.com", "MAIL")

fun main() {
    run(true)
}

fun run(ssl: Boolean) {
    val smtpClient = if (ssl) SmtpClientSSL() else SmtpClient()
    smtpClient.use { client ->
        client.connect(arguments.getOrNull(0) ?: throw IllegalArgumentException("Expected 'SMTP server' as the first argument"))
        client.send {
            this.addressFrom = arguments.getOrNull(1) ?: throw IllegalArgumentException("Expected email 'FROM' as the second argument")
            this.addressTo = arguments.getOrNull(2) ?: throw IllegalArgumentException("Expected email 'TO' as the third argument")
            this.content = arguments.drop(3).joinToString(" ")
        }
    }
}
