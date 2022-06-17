package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.Method

interface ContractInvocationMethodExtractor {
    fun extractMethod(contractTransaction: ContractTransaction): Method
}
