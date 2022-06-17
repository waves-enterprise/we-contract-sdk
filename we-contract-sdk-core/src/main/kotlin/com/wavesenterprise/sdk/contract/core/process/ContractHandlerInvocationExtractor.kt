package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.domain.ContractKeys
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.contract.CallContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.CreateContractTransaction
import java.lang.reflect.Method

class ContractHandlerInvocationExtractor<T>(
    private val contractHandlerType: Class<T>,
) : ContractInvocationMethodExtractor {

    private val initMethodByNameMap: Map<String, Method>
    private val actionMethodByNameMap: Map<String, Method>

    init {
        requireNotNull(contractHandlerType.getAnnotation(ContractHandler::class.java)) {
            "Class ${contractHandlerType.canonicalName} doesn't have @ContractHandler annotation on it"
        }
        val initMethods: MutableList<Pair<String, Method>> = mutableListOf()
        val actionMethods: MutableList<Pair<String, Method>> = mutableListOf()
        contractHandlerType.methods.forEach { method ->
            method.getAnnotation(ContractInit::class.java)?.also {
                initMethods.add(it.name.ifBlank { method.name } to method)
            }
            method.getAnnotation(ContractAction::class.java)?.also {
                actionMethods.add(it.name.ifBlank { method.name } to method)
            }
        }
        if (initMethods.isEmpty() && actionMethods.isEmpty()) {
            throw IllegalArgumentException(
                "Class ${contractHandlerType.canonicalName} had neither @ContractInit" +
                    " nor @ContractAction annotated methods"
            )
        }
        initMethodByNameMap = initMethods.toMap()
        actionMethodByNameMap = actionMethods.toMap()
    }

    override fun extractMethod(contractTransaction: ContractTransaction): Method {
        val actionName = requireNotNull(
            contractTransaction.params.firstOrNull { it.key.value == ContractKeys.ACTION_KEY }?.run {
                when (val dataEntryValue = value) {
                    is DataValue.StringDataValue -> dataEntryValue.value
                    else -> throw IllegalArgumentException(
                        "Only string data values a supported for params with 'action' key. " +
                            "ContractHandler - ${contractHandlerType.canonicalName}. Key value $value"
                    )
                }
            }
        ) {
            "Key 'action' has not been found in params of contract tx with ID = ${contractTransaction.id}. " +
                "Contract ID = ${contractTransaction.contractId} "
        }
        return when (contractTransaction) {
            is CallContractTransaction -> requireNotNull(actionMethodByNameMap[actionName]) {
                "ContractAction ${errorMessagePart(actionName)}"
            }
            is CreateContractTransaction -> requireNotNull(initMethodByNameMap[actionName]) {
                "ContractInit ${errorMessagePart(actionName)}"
            }
        }
    }

    private fun errorMessagePart(actionName: String) =
        "named $actionName hasn't been found in class ${contractHandlerType.canonicalName}"
}
