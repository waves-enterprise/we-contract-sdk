package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.contract.core.state.LocalValidationContextManager

class ContractLocalValidationExecutor(
    private val localValidationContextManager: LocalValidationContextManager,
) {
    fun <T> withLocalValidationScope(
        block: () -> T,
    ): T {
        localValidationContextManager.enablePreValidationContextCache()
        return try {
            block()
        } finally {
            localValidationContextManager.disablePreValidationContextCache()
            localValidationContextManager.clearContext()
        }
    }
}
