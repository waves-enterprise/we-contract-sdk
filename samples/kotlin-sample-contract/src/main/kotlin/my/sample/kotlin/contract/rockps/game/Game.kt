package my.sample.kotlin.contract.rockps.game

import my.sample.kotlin.contract.rockps.util.hash

data class Game(
    val players: Map<String, Player>,
    var winner: Player? = null,
    var status: GameStatus = GameStatus.ACTIVE,
) {

    fun reveal(address: String, salt: String) {
        val player = requireNotNull(players[address]) {
            "Address $address isn't among players of the game"
        }
        val hashedAnswers = getHashedAnswers(salt)
        val answer = requireNotNull(hashedAnswers[player.hashedAnswer]) {
            "Not found matching answer for salt"
        }
        player.answer = answer
        if (allPlayersAnswered()) {
            finish()
        }
    }

    private fun finish() {
        status = GameStatus.FINISHED
        if (allPlayersHaveTheSameAnswers()) {
            return
        }
        winner = players.values.maxWithOrNull { a: Player, b: Player ->
            AnswerComparator().compare(a.answer!!, b.answer!!)
        } ?: throw IllegalStateException(
            "Winner could not be determined"
        )
    }

    private fun allPlayersHaveTheSameAnswers(): Boolean {
        val differentAnswers = players.values.map { obj: Player -> obj.answer }.toSet()
        return differentAnswers.size == 1
    }

    private fun allPlayersAnswered() = players.values.all { player: Player -> player.answer != null }

    private fun getHashedAnswers(salt: String) =
        AnswerType.values().associateBy { hash(it.toString() + "_" + salt) }

    internal class AnswerComparator : Comparator<AnswerType> {
        override fun compare(o1: AnswerType, o2: AnswerType): Int {
            return if (o1 == AnswerType.SCISSORS && o2 == AnswerType.ROCK || o1 == AnswerType.ROCK && o2 == AnswerType.SCISSORS) {
                -o1.compareTo(o2)
            } else o1.compareTo(o2)
        }
    }
}