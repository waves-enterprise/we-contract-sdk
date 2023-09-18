package com.wavesenterprise.sdk.contract.core.node

import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.keys.ContractKeyRequest
import com.wavesenterprise.sdk.node.domain.contract.keys.ContractKeysRequest
import com.wavesenterprise.sdk.node.domain.contract.keys.KeysFilter
import java.util.Optional

class BlockingClientNodeContractStateValuesProvider(
    private val contractService: ContractService,
) : NodeContractStateValuesProvider {

    override fun getForKey(contractId: ContractId, key: String): Optional<DataEntry> =
        contractService.getContractKey(ContractKeyRequest(contractId, key))

    override fun getForKeys(contractId: ContractId, keys: Set<String>): List<DataEntry> =
        contractService.getContractKeys(
            ContractKeysRequest(
                contractId = contractId,
                keysFilter = KeysFilter(keys.toList()),
                offset = null,
                limit = null,
            )
        )
}
