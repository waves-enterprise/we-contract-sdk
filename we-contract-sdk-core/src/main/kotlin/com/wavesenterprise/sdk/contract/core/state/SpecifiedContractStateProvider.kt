package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState

class SpecifiedContractStateProvider(
    private val contractState: ContractState
) : ContractStateProvider {
    override fun getContractState(): ContractState = contractState
}
