package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.domain.DefaultContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.wrc.WRC12.CONTRACT_ID_KEY
import com.wavesenterprise.sdk.contract.api.wrc.WRC12.CONTRACT_META_KEY
import com.wavesenterprise.sdk.contract.api.wrc.WRC12Meta
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.Constructor

class ContractHandlerFactoryImpl<T>(
    private val contractState: ContractState,
    private val contractHandlerType: Class<T>
) : ContractHandlerFactory<T> {

    override fun createHandler(tx: ContractTransaction): T {
        val constructors = contractHandlerType.constructors

        require(constructors.size == 1) {
            "ContractHandler ${contractHandlerType.canonicalName} class should have exactly one public constructor"
        }
        return constructors.first().newInstanceFor(tx, contractState)
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    private fun Constructor<*>.newInstanceFor(
        tx: ContractTransaction,
        contractState: ContractState
    ) = run {
        if (parameterCount == 0) {
            newInstance() as T
        } else {
            val invocationParams = Array<Any?>(parameterCount) { null }
            parameterTypes.forEachIndexed { idx, paramClass ->
                val arg = when (paramClass) {
                    ContractState::class.java -> contractState
                    ContractTransaction::class.java -> tx
                    ContractCall::class.java -> DefaultContractCall(tx)
                    else -> throw IllegalArgumentException(
                        "Contract handler doesn't support ${paramClass.canonicalName} in its constructor arguments. " +
                            "Contract handler class - ${contractHandlerType.canonicalName}"
                    )
                }
                putWrc12MetaIntoResult(contractState, contractHandlerType, tx)
                invocationParams[idx] = arg
            }
            newInstance(*invocationParams) as T
        }
    }

    private fun putWrc12MetaIntoResult(
        contractState: ContractState,
        contractHandler: Class<*>,
        tx: ContractTransaction,
    ) {
        val meta = WRC12Meta(
            lang = "java",
            interfaces = contractHandler.interfaces.toList().map { it.name },
            impls = listOf(contractHandler.name),
        )
        if (tx.type === TxType.CREATE_CONTRACT) {
            contractState.put(CONTRACT_ID_KEY, tx.id)
        }
        contractState.put(CONTRACT_META_KEY, meta)
    }
}
