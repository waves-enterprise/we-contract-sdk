package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState

interface ContractStateProvider {
    fun getContractState(): ContractState
}
