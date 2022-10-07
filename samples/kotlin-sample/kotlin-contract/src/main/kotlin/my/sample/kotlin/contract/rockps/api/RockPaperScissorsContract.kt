package my.sample.kotlin.contract.rockps.api

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import my.sample.kotlin.contract.rockps.game.request.CreateGameRequest
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.game.request.RevealRequest

interface RockPaperScissorsContract {

    @ContractInit
    fun createGame(createGameRequest: CreateGameRequest)

    @ContractAction
    fun play(registerPlayerRequest: PlayRequest)

    @ContractAction
    fun reveal(revealRequest: RevealRequest)

    object Keys {
        const val PLAYER_ADDRESSES_KEY = "PLAYER_ADDRESSES"
        const val PLAYERS_MAPPING_PREFIX = "PLAYERS"
        const val GAME_KEY = "GAME"
        const val GAME_STATUS_KEY = "GAME_STATUS"
        const val GAME_WINNER_ADDR_KEY = "GAME_WINNER_ADDRESS"
        const val CREATED_DATE_KEY = "CREATED_DATE"
    }
}
