package my.sample.kotlin.contract.rockps.service

import com.wavesenterprise.sdk.node.domain.tx.ContractTx
import my.sample.kotlin.contract.rockps.controller.dto.TxDto
import my.sample.kotlin.contract.rockps.game.request.CreateGameRequest
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.game.request.RevealRequest

interface GameService {

    fun createGame(createGameRequest: CreateGameRequest, address: String, password: String): ContractTx

    fun play(playRequest: PlayRequest, address: String, password: String, contractId: String): ContractTx

    fun reveal(revealRequest: RevealRequest, address: String, password: String, contractId: String): ContractTx
}