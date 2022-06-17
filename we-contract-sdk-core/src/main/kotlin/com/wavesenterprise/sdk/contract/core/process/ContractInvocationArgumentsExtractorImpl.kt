package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.domain.ContractKeys
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.Method

class ContractInvocationArgumentsExtractorImpl(
    private val contractFromDataEntryConverter: ContractFromDataEntryConverter,
) : ContractInvocationArgumentsExtractor {

    override fun extractArguments(
        method: Method,
        contractTx: ContractTransaction,
    ): List<Any> {
        val paramList = contractTx.params.filter { it.key.value != ContractKeys.ACTION_KEY }
        require(paramList.size == method.parameterCount) {
            "Mismatched parameter count for method ${method.fullName()} and transaction with " +
                "ID = ${contractTx.id.asBase58String()}. Method has ${method.parameterCount} args, " +
                "transaction has ${paramList.size}"
        }
        return paramList.mapIndexed { index, dataEntry ->
            contractFromDataEntryConverter.convert(dataEntry, method.parameterTypes[index])
        }
    }

    private fun Method.fullName() = "${declaringClass.canonicalName}#$name"
}
