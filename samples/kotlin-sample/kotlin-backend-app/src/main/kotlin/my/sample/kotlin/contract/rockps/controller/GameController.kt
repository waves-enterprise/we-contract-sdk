package my.sample.kotlin.contract.rockps.controller

import my.sample.kotlin.contract.rockps.controller.dto.CreateGameRequestDto
import my.sample.kotlin.contract.rockps.controller.dto.PlayRequestDto
import my.sample.kotlin.contract.rockps.controller.dto.RevealRequestDto
import my.sample.kotlin.contract.rockps.controller.dto.toContractDto
import my.sample.kotlin.contract.rockps.controller.dto.toDto
import my.sample.kotlin.contract.rockps.game.request.PlayRequest
import my.sample.kotlin.contract.rockps.service.GameService
import my.sample.kotlin.contract.rockps.util.hash
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/game")
class GameController(
    private val gameService: GameService,
) {

    @PostMapping("/create-game")
    fun createGame(
        @RequestHeader("X-we-tx-sender-address") address: String,
        @RequestHeader("X-we-tx-sender-password") password: String = "",
        @RequestBody createGameRequest: CreateGameRequestDto,
    ) = gameService.createGame(createGameRequest.toContractDto(), address, password).toDto()

    @PostMapping("/play")
    fun play(
        @RequestHeader("X-we-tx-sender-address") address: String,
        @RequestHeader("X-we-tx-sender-password") password: String = "",
        @RequestHeader("X-we-tx-sender-contract-id") contractId: String,
        @RequestBody playRequest: PlayRequestDto,
    ) = gameService.play(playRequest.toContractDto(), address, password, contractId).toDto()


    @PostMapping("/reveal")
    fun reveal(
        @RequestHeader("X-we-tx-sender-address") address: String,
        @RequestHeader("X-we-tx-sender-password") password: String = "",
        @RequestHeader("X-we-tx-sender-contract-id") contractId: String,
        @RequestBody revealRequest: RevealRequestDto,
    ) = gameService.reveal(revealRequest.toContractDto(), address, password, contractId).toDto()

    fun PlayRequestDto.toContractDto() =
        PlayRequest(
            hashedAnswer = hash("${answer.name}_$salt"),
        )
}
