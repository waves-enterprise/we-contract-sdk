package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction

class ContractTransactionProcessor(
    private val contractInvocationMethodExtractor: ContractInvocationMethodExtractor,
    private val contractInvocationArgumentsExtractor: ContractInvocationArgumentsExtractor,
    private val contractHandlerFactory: ContractHandlerFactory<*>,
) {

    fun process(contractTransaction: ContractTransaction) {
        val contractHandler = contractHandlerFactory.createHandler(contractTransaction)

        val extractMethod = contractInvocationMethodExtractor.extractMethod(contractTransaction)
        val extractedArguments =
            contractInvocationArgumentsExtractor.extractArguments(extractMethod, contractTransaction)
        extractMethod.invoke(contractHandler, *extractedArguments.toTypedArray())
    }
}
