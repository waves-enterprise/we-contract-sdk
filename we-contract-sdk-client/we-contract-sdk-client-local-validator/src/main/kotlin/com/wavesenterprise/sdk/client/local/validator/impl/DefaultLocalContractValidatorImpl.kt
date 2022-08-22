package com.wavesenterprise.sdk.client.local.validator.impl

import com.wavesenterprise.sdk.client.local.validator.LocalContractValidator
import com.wavesenterprise.sdk.contract.api.exception.ContractPreValidationException
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerFactoryImpl
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerInvocationExtractor
import com.wavesenterprise.sdk.contract.core.process.ContractInvocationArgumentsExtractorImpl
import com.wavesenterprise.sdk.contract.core.state.factory.ContractStateFactory
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class DefaultLocalContractValidatorImpl(
    private val contractHandlerType: Class<*>,
    private val contractStateFactory: ContractStateFactory,
    contractFromDataEntryConverter: ContractFromDataEntryConverter,
) : LocalContractValidator {
    private val contractHandlerInvocationExtractor: ContractHandlerInvocationExtractor<*> =
        ContractHandlerInvocationExtractor(contractHandlerType)

    private val contractArgumentInvocationExtractor =
        ContractInvocationArgumentsExtractorImpl(contractFromDataEntryConverter)

    override fun validate(contractTransaction: ContractTransaction): List<DataEntry> {
        val contractState = contractStateFactory.buildContractState(
            contractTransaction.contractId
        )
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState = contractState,
            contractHandlerType = contractHandlerType,
        )
        val contractHandler = contractHandlerFactory.createHandler(contractTransaction)

        val method = contractHandlerInvocationExtractor.extractMethod(contractTransaction)
        val args = contractArgumentInvocationExtractor.extractArguments(method, contractTransaction)
        preValidate(method, args.toTypedArray(), contractHandler, contractTransaction.contractId)
        return contractState.results()
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
                message = "Error on prevalidation of contract invoke with params : \n" +
                    " methodName : ${method.name} \n" +
                    " implementation : $implementation \n" +
                    " args : $args \n" +
                    " contractId : ${contractId.asBase58String()}",
                cause = e.targetException,
                method = method,
                implementation = implementation,
                args = args,
                contractId = contractId.asBase58String(),
            )
        } catch (e: IllegalAccessException) {
            throw ContractPreValidationException(
                message = "Error on prevalidation of contract invoke with params : \n" +
                    " methodName : ${method.name} \n" +
                    " implementation : $implementation \n" +
                    " args : $args \n" +
                    " contractId : ${contractId.asBase58String()}",
                cause = e,
                method = method,
                implementation = implementation,
                args = args,
                contractId = contractId.asBase58String(),
            )
        }
    }
}
