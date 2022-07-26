package my.sample.kotlin.contract.rockps

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.test.data.TestDataProvider.Companion.address
import com.wavesenterprise.sdk.contract.test.data.TestDataProvider.Companion.contractTransaction
import com.wavesenterprise.sdk.contract.test.state.ContractTestStateFactory
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.TxType
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.GAME_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.GAME_STATUS_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.GAME_WINNER_ADDR_KEY
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.PLAYERS_MAPPING_PREFIX
import my.sample.kotlin.contract.rockps.api.RockPaperScissorsContract.Keys.PLAYER_ADDRESSES_KEY
import my.sample.kotlin.contract.rockps.game.AnswerType
import my.sample.kotlin.contract.rockps.game.Game
import my.sample.kotlin.contract.rockps.game.GameStatus
import my.sample.kotlin.contract.rockps.game.Player
import my.sample.kotlin.contract.rockps.game.request.CreateGameRequest
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.game.request.RevealRequest
import my.sample.kotlin.contract.rockps.util.hash
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RockPaperScissorsContractTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `should throw error when more than two players`() {
        val player1 = address()
        val player2 = address()
        val player3 = address()
        val players = listOf(player1, player2, player3)
        val testState = ContractTestStateFactory.state(objectMapper)
        val rockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(address(), TxType.CREATE_CONTRACT)
        )
        assertThrows(IllegalArgumentException::class.java) {
            rockPaperScissorsContract.createGame(
                gameRequest(players)
            )
        }
    }

    @Test
    fun `should create game`() {
        val player1 = address()
        val player2 = address()
        val players = listOf(player1, player2)
        val testState = ContractTestStateFactory.state(objectMapper)
        val rockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(address(), TxType.CREATE_CONTRACT)
        )
        rockPaperScissorsContract.createGame(
            gameRequest(players)
        )
        val playerAddresses = testState[PLAYER_ADDRESSES_KEY, object : TypeReference<Set<String>>() {}]
        assertEquals(players.size, playerAddresses.size)
        assertFalse(testState.tryGet(GAME_KEY, Game::class.java).isPresent)
    }

    @Test
    fun `should throw error when playing by not known address`() {
        val player1 = address()
        val player2: Address = address()
        val sender: Address = address()
        val players: List<Address> = listOf(player1, player2)
        val testState: ContractState = ContractTestStateFactory.state(objectMapper)
        val rockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(sender, TxType.CALL_CONTRACT)
        )
        rockPaperScissorsContract.createGame(
            gameRequest(players)
        )
        assertThrows(
            IllegalAccessError::class.java
        ) { rockPaperScissorsContract.play(PlayRequest("someHash")) }
    }

    private fun gameRequest(players: List<Address>): CreateGameRequest {
        return CreateGameRequest(
            players.map { address -> CreateGameRequest.Player(address.asBase58String()) }
        )
    }

    @Test
    fun `should play`() {
        val player1 = address()
        val player2 = address()
        val sender = address()
        val players = listOf(player1, player2)
        val testState = ContractTestStateFactory.state(objectMapper)
        val rockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(sender, TxType.CALL_CONTRACT)
        )
        rockPaperScissorsContract.createGame(
            gameRequest(players)
        )
        assertThrows(
            IllegalAccessError::class.java
        ) { rockPaperScissorsContract.play(PlayRequest("someHash")) }
    }

    @Test
    fun `should play game`() {
        val currentAddress = address()
        val testState = ContractTestStateFactory.state(objectMapper)
        val players: Mapping<Player> =
            testState.getMapping(
                Player::class.java,
                PLAYERS_MAPPING_PREFIX
            )
        val rockPaperScissorsContract =
            RockPaperScissorsContractImpl(
                testState,
                contractTransaction(currentAddress, TxType.CREATE_CONTRACT)
            )
        val currentAddressStr = currentAddress.asBase58String()
        testState.put(
            PLAYER_ADDRESSES_KEY,
            listOf(currentAddressStr)
        )
        val hashedAnswer = "hashed"

        rockPaperScissorsContract.play(PlayRequest(hashedAnswer))

        val player: Player = players[currentAddressStr]
        assertEquals(hashedAnswer, player.hashedAnswer)
    }

    @Test
    fun `should reveal result`() {
        val currentAddress = address()
        val testState = ContractTestStateFactory.state(objectMapper)
        val players = testState.getMapping(Player::class.java, PLAYERS_MAPPING_PREFIX)
        val rockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(currentAddress, TxType.CREATE_CONTRACT)
        )
        val currentAddressStr = currentAddress.asBase58String()
        testState.put(PLAYER_ADDRESSES_KEY, listOf(currentAddressStr))
        val salt = "asdfb23"
        val answerType = AnswerType.PAPER
        val hashedAnswer = hash(answerType.toString() + "_" + salt)
        rockPaperScissorsContract.play(PlayRequest(hashedAnswer))
        rockPaperScissorsContract.reveal(RevealRequest(salt))
        val player = players[currentAddressStr]
        assertEquals(player.answer, AnswerType.PAPER)
    }

    @Test
    fun `should pass integration test`() {
        val player1 = address()
        val player1Salt = "salt1234"
        val player1AnswerType = AnswerType.PAPER
        val player2 = address()
        val player2Salt = "salt567"
        val player2AnswerType = AnswerType.SCISSORS
        val contractCreator = address()
        val testState = ContractTestStateFactory.state(objectMapper)
        val rockPaperScissorsContract: RockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(contractCreator, TxType.CREATE_CONTRACT)
        )
        rockPaperScissorsContract.createGame(
            gameRequest(listOf(player1, player2))
        )
        val rockPaperScissorsContractForPlayer1 = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(player1, TxType.CALL_CONTRACT)
        )
        rockPaperScissorsContractForPlayer1.play(PlayRequest(hash(player1AnswerType.toString() + "_" + player1Salt)))
        val rockPaperScissorsContractForPlayer2: RockPaperScissorsContract = RockPaperScissorsContractImpl(
            testState,
            contractTransaction(player2, TxType.CALL_CONTRACT)
        )
        rockPaperScissorsContractForPlayer2.play(PlayRequest(hash(player2AnswerType.toString() + "_" + player2Salt)))
        rockPaperScissorsContractForPlayer1.reveal(RevealRequest(player1Salt))
        rockPaperScissorsContractForPlayer2.reveal(RevealRequest(player2Salt))
        val gameStatus = testState[GAME_STATUS_KEY, GameStatus::class.java]
        assertEquals(GameStatus.FINISHED, gameStatus)
        val winnerAddress = testState[GAME_WINNER_ADDR_KEY, String::class.java]
        assertEquals(player2.asBase58String(), winnerAddress)
    }
}
