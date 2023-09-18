package com.wavesenterprise.sdk.client.local.validator.impl

import com.wavesenterprise.sdk.client.local.validator.LocalContractValidator
import com.wavesenterprise.sdk.contract.api.exception.ContractPreValidationException
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerFactoryImpl
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerInvocationExtractor
import com.wavesenterprise.sdk.contract.core.process.ContractInvocationArgumentsExtractorImpl
import com.wavesenterprise.sdk.contract.core.state.ContractStateWithCachedExternalContracts
import com.wavesenterprise.sdk.contract.core.state.LocalValidationContext
import com.wavesenterprise.sdk.contract.core.state.LocalValidationContextManager
import com.wavesenterprise.sdk.contract.core.state.factory.ContractStateFactory
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class DefaultLocalContractValidatorImpl(
    private val contractHandlerType: Class<*>,
    private val contractStateFactory: ContractStateFactory,
    private val localValidationContextManager: LocalValidationContextManager,
    contractFromDataEntryConverter: ContractFromDataEntryConverter,
) : LocalContractValidator {
    private val contractHandlerInvocationExtractor: ContractHandlerInvocationExtractor<*> =
        ContractHandlerInvocationExtractor(contractHandlerType)

    private val contractArgumentInvocationExtractor =
        ContractInvocationArgumentsExtractorImpl(contractFromDataEntryConverter)

    override fun validate(contractTransaction: ContractTransaction): ContractState {
        val contractId: ContractId = contractTransaction.contractId
        val localValidationContext: LocalValidationContext = localValidationContextManager.getContext()
        val contractState = buildContractState(contractId, localValidationContext)
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState = contractState,
            contractHandlerType = contractHandlerType,
        )
        val contractHandler = contractHandlerFactory.createHandler(contractTransaction)

        val method = contractHandlerInvocationExtractor.extractMethod(contractTransaction)
        val args = contractArgumentInvocationExtractor.extractArguments(method, contractTransaction)
        preValidate(method, args.toTypedArray(), contractHandler, contractTransaction.contractId)
        if (localValidationContext.enabled) {
            putToContextCache(
                contractId = contractId,
                contractState = contractState,
            )
        }
        return contractState
    }

    private fun buildContractState(
        contractId: ContractId,
        localValidationContext: LocalValidationContext,
    ): ContractState {
        val newContractState = contractStateFactory.buildContractState(contractId)
        val contractState = if (localValidationContext.enabled) {
            ContractStateWithCachedExternalContracts(
                delegate = localValidationContext.stateCache[contractId] ?: newContractState,
                externalStates = localValidationContextManager.getContext().stateCache,
            )
        } else {
            newContractState
        }
        return contractState
    }

    private fun putToContextCache(
        contractId: ContractId,
        contractState: ContractState,
    ) {
        with(localValidationContextManager) {
            val state = ContractStateWithCachedExternalContracts(
                delegate = contractState,
                externalStates = getContext().stateCache,
            )
            addToContext(contractId, state)
        }
    }

    private fun preValidate(
        method: Method,
        args: Array<Any>,
        implementation: Any,
        contractId: ContractId,
    ) {
        try {
            method.invoke(implementation, *args)
        } catch (e: InvocationTargetException) {
            throw ContractPreValidationException(
                message = "Error on prevalidation of contract invoke with params : \n" + " methodName : ${method.name} \n" + " implementation : $implementation \n" + " args : $args \n" + " contractId : ${contractId.asBase58String()}",
                cause = e.targetException,
                method = method,
                implementation = implementation,
                args = args,
                contractId = contractId.asBase58String(),
            )
        } catch (e: IllegalAccessException) {
            throw ContractPreValidationException(
                message = "Error on prevalidation of contract invoke with params : \n" + " methodName : ${method.name} \n" + " implementation : $implementation \n" + " args : $args \n" + " contractId : ${contractId.asBase58String()}",
                cause = e,
                method = method,
                implementation = implementation,
                args = args,
                contractId = contractId.asBase58String(),
            )
        }
    }
}
