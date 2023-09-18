package com.wavesenterprise.sdk.contract.core.state

import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.node.domain.contract.ContractId

class ThreadLocalLocalValidationContextManager : LocalValidationContextManager {
    override fun getContext(): LocalValidationContext = contextHolder.get()

    override fun enablePreValidationContextCache() {
        getContext().enabled = true
    }

    override fun disablePreValidationContextCache() {
        getContext().enabled = false
    }

    override fun clearContext() {
        with(getContext()) {
            stateCache.clear()
        }
    }

    override fun addToContext(contractId: ContractId, contractState: ContractState) {
        getContext().stateCache[contractId] = contractState
    }

    companion object {
        private val contextHolder: ThreadLocal<LocalValidationContext> = ThreadLocal.withInitial {
            LocalValidationContext(
                enabled = false,
                stateCache = hashMapOf(),
            )
        }
    }
}
