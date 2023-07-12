package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.ContractStateReader
import com.wavesenterprise.sdk.node.domain.contract.ContractId

class ContractStateWithCachedExternalContracts(
    val delegate: ContractState,
    val externalStates: Map<ContractId, ContractState>,
) : ContractState by delegate {
    override fun external(contractId: ContractId): ContractStateReader {
        val contractState = externalStates[contractId]
        return contractState ?: delegate.external(contractId)
    }
}
