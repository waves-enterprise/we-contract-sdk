package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId

fun NodeContractStateValuesProvider.getForKeyAsNullable(contractId: ContractId, key: String): DataEntry? =
    getForKey(contractId, key).orElseGet { null }
