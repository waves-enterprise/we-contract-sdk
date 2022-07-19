package com.wavesenterprise.sdk.contract.core.state.factory

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.core.state.ContractStateReaderIml
import com.wavesenterprise.sdk.node.domain.contract.ContractId

class ExternalContractStateFactory(
    private val nodeContractStateValuesProvider: NodeContractStateValuesProvider,
    private val contractFromDataEntryConverter: ContractFromDataEntryConverter,
) : ContractStateReaderFactory {

    override fun buildContractState(contractId: ContractId) =
        ContractStateReaderIml(
            contractId = contractId,
            nodeContractStateValuesProvider = nodeContractStateValuesProvider,
            contractFromDataEntryConverter = contractFromDataEntryConverter,
        )
}
