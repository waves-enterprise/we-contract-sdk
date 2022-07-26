package my.sample.kotlin.contract.rockps.game

data class Player(
    val address: String,
    val hashedAnswer: String,
    var answer: AnswerType? = null,
)
