package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.node.domain.contract.ContractId

data class LocalValidationContext(
    var enabled: Boolean,
    val stateCache: HashMap<ContractId, ContractState>,
)
