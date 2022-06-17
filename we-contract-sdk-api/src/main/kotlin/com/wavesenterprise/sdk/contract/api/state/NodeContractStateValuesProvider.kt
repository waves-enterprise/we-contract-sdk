package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import java.util.Optional

interface NodeContractStateValuesProvider {
    fun getForKey(contractId: ContractId, key: String): Optional<DataEntry>
    fun getForKeys(contractId: ContractId, keys: Set<String>): List<DataEntry>
}
