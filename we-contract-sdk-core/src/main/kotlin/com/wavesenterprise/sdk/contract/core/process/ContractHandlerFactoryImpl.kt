package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.domain.DefaultContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
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
                invocationParams[idx] = arg
            }
            newInstance(*invocationParams) as T
        }
    }
}
