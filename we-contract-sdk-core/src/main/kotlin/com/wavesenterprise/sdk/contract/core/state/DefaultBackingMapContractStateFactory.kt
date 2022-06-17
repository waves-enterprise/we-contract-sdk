package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId

class DefaultBackingMapContractStateFactory(
    private val nodeContractStateValuesProvider: NodeContractStateValuesProvider,
    private val contractFromDataEntryConverter: ContractFromDataEntryConverter,
    private val contractToDataValueConverter: ContractToDataValueConverter,
) : ContractStateFactory {

    override fun buildContractState(contractId: ContractId): ContractState {
        val backingMapForState: MutableMap<String, DataEntry> = hashMapOf()
        val contractStateReader = ContractStateReaderIml(
            contractId = contractId,
            nodeContractStateValuesProvider = nodeContractStateValuesProvider,
            contractFromDataEntryConverter = contractFromDataEntryConverter,
            backingMap = backingMapForState
        )
        return ContractStateImpl(
            contractStateReader = contractStateReader,
            contractToDataValueConverter = contractToDataValueConverter,
            backingMap = backingMapForState
        )
    }
}
