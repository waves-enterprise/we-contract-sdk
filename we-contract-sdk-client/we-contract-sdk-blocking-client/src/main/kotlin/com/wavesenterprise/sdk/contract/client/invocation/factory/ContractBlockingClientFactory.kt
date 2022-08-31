package com.wavesenterprise.sdk.contract.client.invocation.factory

import com.wavesenterprise.sdk.client.local.validator.impl.DefaultLocalContractValidatorImpl
import com.wavesenterprise.sdk.contract.client.invocation.ParamsBuilder
import com.wavesenterprise.sdk.contract.client.invocation.TxTypeResolver
import com.wavesenterprise.sdk.contract.client.invocation.impl.ParamsBuilderImpl
import com.wavesenterprise.sdk.contract.client.invocation.impl.TxTypeResolverImpl
import com.wavesenterprise.sdk.contract.core.node.BlockingClientNodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.core.state.factory.DefaultBackingMapContractStateFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonFromDataEntryConverter
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.domain.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.converter.toContractTransaction
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.node.domain.tx.ContractTx
import com.wavesenterprise.sdk.tx.signer.TxSigner
import java.lang.reflect.Proxy

class ContractBlockingClientFactory<T, S>(
    private val contractClass: Class<T>?,
    private val contractInterface: Class<S>,
    private val txSigner: TxSigner,
    private val converterFactory: JacksonConverterFactory,
    private val contractClientProperties: ContractClientParams,
    private val contractSignRequestBuilder: ContractSignRequestBuilder,
    nodeBlockingServiceFactory: NodeBlockingServiceFactory,
) {
    private val txService: TxService = nodeBlockingServiceFactory.txService()
    private val contractService: ContractService = nodeBlockingServiceFactory.contractService()

    @Suppress("UNCHECKED_CAST")
    fun createContractClient(): S {
        val nodeContractStateProvider = BlockingClientNodeContractStateValuesProvider(
            contractService = contractService
        )

        val toDataValueConverter = converterFactory.toDataValueConverter()
        val fromDataEntryConverter = converterFactory.fromDataEntryConverter()

        val contractStateFactory = DefaultBackingMapContractStateFactory(
            nodeContractStateValuesProvider = nodeContractStateProvider,
            contractFromDataEntryConverter = fromDataEntryConverter,
            contractToDataValueConverter = toDataValueConverter,
        )

        val paramsBuilder: ParamsBuilder = ParamsBuilderImpl(toDataValueConverter)
        val txTypeResolver: TxTypeResolver = TxTypeResolverImpl()
        val invocationHandlerFactory = ContractHandlerInvocationHandlerFactory(paramsBuilder, txTypeResolver)
        val contractHandlerInvocationHandler =
            invocationHandlerFactory.handleContractInvocation { params: List<DataEntry>, txType: TxType ->
                val signRequest = contractSignRequestBuilder
                    .params(params)
                    .build(txType)
                val tx = txSigner.sign(signRequest)
                handleLocalContractValidation(contractStateFactory, fromDataEntryConverter, tx)
                txService.broadcast(tx)
            }

        return Proxy.newProxyInstance(
            contractInterface.classLoader,
            arrayOf(contractInterface),
            contractHandlerInvocationHandler,
        ) as S
    }

    private fun handleLocalContractValidation(
        contractStateFactory: DefaultBackingMapContractStateFactory,
        fromDataEntryConverter: JacksonFromDataEntryConverter,
        tx: ContractTx
    ) {
        if (contractClientProperties.localValidationEnabled) {
            contractClass?.also {
                DefaultLocalContractValidatorImpl(
                    contractHandlerType = contractClass,
                    contractStateFactory = contractStateFactory,
                    contractFromDataEntryConverter = fromDataEntryConverter,
                ).validate(tx.toContractTransaction())
            } ?: throw IllegalArgumentException("Contract implementation is required for local validation")
        }
    }
}
