package my.sample.kotlin.contract.rockps

import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.CREATED_DATE_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.GAME_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.GAME_STATUS_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.GAME_WINNER_ADDR_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.PLAYERS_MAPPING_PREFIX
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.PLAYER_ADDRESSES_KEY
import my.sample.kotlin.contract.rockps.game.Game
import my.sample.kotlin.contract.rockps.game.GameStatus
import my.sample.kotlin.contract.rockps.game.Player
import my.sample.kotlin.contract.rockps.game.request.CreateGameRequest
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.game.request.RevealRequest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@ContractHandler
class RockPaperScissorsContractImpl(
    private val contractState: ContractState,
    private val tx: ContractTransaction,
): RockPaperScissorsContract {

    private val players = contractState.getMapping(Player::class.java, PLAYERS_MAPPING_PREFIX)

    override fun createGame(createGameRequest: CreateGameRequest) {
        require(createGameRequest.players.size <= 2) { "Currently only two players are supported" }
        contractState.put(
            CREATED_DATE_KEY,
            txTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        )
        contractState.put(
            PLAYER_ADDRESSES_KEY,
            createGameRequest.players.map(CreateGameRequest.Player::address).toCollection(mutableSetOf()),
        )
    }

    override fun play(registerPlayerRequest: PlayRequest) {
        check(!contractState.tryGet(GAME_KEY, Game::class.java).isPresent) { "Game has already started" }
        val senderAddress = senderAddress()
        val addresses = getPlayerAddresses()
        val txSenderAddress: String = txSender()
        if (!addresses.contains(txSenderAddress)) {
            throw IllegalAccessError("Address $txSenderAddress is not present in the players of this game")
        }
        players.put(senderAddress(), Player(senderAddress, registerPlayerRequest.hashedAnswer))
        if (players.hasAll(addresses)) {
            startGame(addresses)
        }
    }

    override fun reveal(revealRequest: RevealRequest) {
        val game = contractState.tryGet(GAME_KEY, Game::class.java)
            .orElseThrow { IllegalStateException("Could not reveal my result for not active game") }
        check(!(game.status === GameStatus.FINISHED)) { "Game has already finished" }
        val currentAddress = txSender()
        game.reveal(currentAddress, revealRequest.salt)
        val player = requireNotNull(game.players[currentAddress]) {
            "Tx Sender with address $currentAddress not found in players"
        }
        players.put(currentAddress, player)
        contractState.put(GAME_KEY, game)
        contractState.put(GAME_STATUS_KEY, game.status)
        game.winner?.let {
            contractState.put(GAME_WINNER_ADDR_KEY, it.address)
        }
    }

    private fun startGame(addresses: Set<String>) {
        val game = Game(players = players.getAll(addresses).values.associateBy { it.address })
        contractState.put(GAME_KEY, game)
        contractState.put(GAME_STATUS_KEY, game.status)
    }

    private fun getPlayerAddresses() = contractState[PLAYER_ADDRESSES_KEY, object : TypeReference<Set<String>>() {}]

    private fun senderAddress() = txSender()

    private fun txSender() = tx.sender.asBase58String()

    private fun txTimestamp() =
        OffsetDateTime.ofInstant(
            Instant.ofEpochMilli(tx.timestamp.utcTimestampMillis),
            ZoneId.of("UTC"),
        )
}
