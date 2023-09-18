package my.sample.kotlin.contract.rockps.controller.dto

import my.sample.kotlin.contract.rockps.game.AnswerType

data class PlayRequestDto(
    val answer: AnswerType,
    val salt: String,
)
