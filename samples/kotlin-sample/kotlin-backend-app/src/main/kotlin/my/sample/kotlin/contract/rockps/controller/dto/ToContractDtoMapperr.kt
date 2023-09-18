package my.sample.kotlin.contract.rockps.controller.dto

import com.wavesenterprise.sdk.node.domain.tx.ContractTx
import my.sample.kotlin.contract.rockps.game.request.CreateGameRequest
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.game.request.RevealRequest

fun CreateGameRequestDto.toContractDto() =
    CreateGameRequest(
        players = players.map { it.toContractDto() }
    )

fun PlayerDto.toContractDto() =
    CreateGameRequest.Player(
        address = address,
    )


fun RevealRequestDto.toContractDto() =
    RevealRequest(
        salt = salt,
    )

fun ContractTx.toDto() = TxDto(
    txId = this.id.asBase58String()
)
