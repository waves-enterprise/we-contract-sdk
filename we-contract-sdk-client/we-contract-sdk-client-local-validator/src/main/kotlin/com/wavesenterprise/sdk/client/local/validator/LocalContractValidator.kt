package com.wavesenterprise.sdk.client.local.validator

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.core.dispatch.ApplierContractTxToState
import com.wavesenterprise.sdk.contract.core.process.ContractHandlerInvocationExtractor
import com.wavesenterprise.sdk.contract.core.process.ContractInvocationArgumentsExtractorImpl
import com.wavesenterprise.sdk.contract.core.state.factory.ContractStateFactory
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class LocalContractValidator(
    private val contractHandlerType : Class<*>,
    private val contractStateFactory: ContractStateFactory,
    private val contractFromDataEntryConverter: ContractFromDataEntryConverter,
) {
    private val contractHandlerInvocationExtractor: ContractHandlerInvocationExtractor<*> =
        ContractHandlerInvocationExtractor(contractHandlerType)
    private val contractArgumentInvocationExtractor =
        ContractInvocationArgumentsExtractorImpl(contractFromDataEntryConverter)

    fun validate(contractTransaction: ContractTransaction) : List<DataEntry> {
        /*
        * cheks?
        * */
        val method = contractHandlerInvocationExtractor.extractMethod(contractTransaction)
        preValidate(
            method = method,
            args = contractArgumentInvocationExtractor.extractArguments(
                method = method,
                contractTx = contractTransaction,
            ).toTypedArray(),
        )

        val applierContractTxToState = ApplierContractTxToState(
            contractHandlerType = contractHandlerType,
            contractInvocationMethodExtractor = contractHandlerInvocationExtractor,
            contractInvocationArgumentsExtractor = contractArgumentInvocationExtractor,
            contractStateFactory = contractStateFactory,
        )
        val contractState = applierContractTxToState.applyContractTxToState(contractTransaction)
        return contractState.results()
    }

    private fun preValidate(
        method: Method,
        args: Array<Any>,
    ) {
        try {
            method.invoke(method, *args)
        } catch (e: InvocationTargetException) {
//            throw ContractPreValidationException(method, implementation, args, contractId, e.targetException)
        } catch (e: IllegalAccessException) {
//            throw ContractPreValidationException(method, implementation, args, contractId, e)
        }
    }
}