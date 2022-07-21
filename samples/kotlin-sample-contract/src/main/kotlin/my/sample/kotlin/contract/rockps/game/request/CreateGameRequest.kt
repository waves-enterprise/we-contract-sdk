package my.sample.kotlin.contract.rockps.game.request

data class CreateGameRequest(
    val players: List<Player>,
) {
    data class Player(
        val address: String,
    )
}
