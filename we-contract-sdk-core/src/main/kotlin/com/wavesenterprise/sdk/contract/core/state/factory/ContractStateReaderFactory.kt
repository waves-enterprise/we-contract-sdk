package com.wavesenterprise.sdk.contract.core.state.factory

import com.wavesenterprise.sdk.contract.api.state.ContractStateReader
import com.wavesenterprise.sdk.node.domain.contract.ContractId

interface ContractStateReaderFactory {
    fun buildContractState(contractId: ContractId): ContractStateReader
}
