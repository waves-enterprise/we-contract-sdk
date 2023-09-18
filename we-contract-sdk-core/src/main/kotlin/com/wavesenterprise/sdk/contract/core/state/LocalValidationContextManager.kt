package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.node.domain.contract.ContractId

interface LocalValidationContextManager {
    fun getContext(): LocalValidationContext
    fun enablePreValidationContextCache()
    fun disablePreValidationContextCache()
    fun clearContext()
    fun addToContext(contractId: ContractId, contractState: ContractState)
}
