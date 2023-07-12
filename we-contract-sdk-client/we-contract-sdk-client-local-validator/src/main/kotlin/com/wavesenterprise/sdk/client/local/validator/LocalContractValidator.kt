package com.wavesenterprise.sdk.client.local.validator

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction

interface LocalContractValidator {

    fun validate(contractTransaction: ContractTransaction): ContractState
}
