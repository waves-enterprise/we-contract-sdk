package com.wavesenterprise.sdk.contract.test

import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import java.util.Optional

object NoOpNodeValuesProvider : NodeContractStateValuesProvider {

    override fun getForKey(contractId: ContractId, key: String): Optional<DataEntry> {
        return Optional.empty()
    }

    override fun getForKeys(contractId: ContractId, keys: Set<String>): List<DataEntry> {
        return emptyList()
    }
}
