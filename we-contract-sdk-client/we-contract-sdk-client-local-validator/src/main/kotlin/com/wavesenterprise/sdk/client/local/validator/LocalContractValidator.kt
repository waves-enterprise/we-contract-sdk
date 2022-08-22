package com.wavesenterprise.sdk.client.local.validator

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction

interface LocalContractValidator {

    fun validate(contractTransaction: ContractTransaction): List<DataEntry>
}
