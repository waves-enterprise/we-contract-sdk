package com.example.demo.contract

import com.wavesenterprise.sdk.contract.api.state.ContractState

class MyCustomLogic(
    val contractState: ContractState,
) {
    fun doSomeLogic(): String {
        return ""
    }
}