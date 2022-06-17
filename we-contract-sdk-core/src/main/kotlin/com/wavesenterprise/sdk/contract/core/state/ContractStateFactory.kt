package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.node.domain.contract.ContractId

interface ContractStateFactory {
    fun buildContractState(contractId: ContractId): ContractState
}
