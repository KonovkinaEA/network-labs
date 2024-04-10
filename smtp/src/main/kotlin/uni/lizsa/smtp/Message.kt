package uni.lizsa.smtp

data class Message(
    var addressFrom: String = "mail1@gmail.com",
    var addressTo: String = "mail2@gmail.com",
    var content: String = ""
)
