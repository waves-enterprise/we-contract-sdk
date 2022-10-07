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
    private val contractClientProperties: ContractClientParams,
    private val contractSignRequestBuilder: ContractSignRequestBuilder,
    converterFactory: JacksonConverterFactory,
    nodeBlockingServiceFactory: NodeBlockingServiceFactory,
) {
    private val txService: TxService = nodeBlockingServiceFactory.txService()
    private val contractService: ContractService = nodeBlockingServiceFactory.contractService()
    private val toDataValueConverter = converterFactory.toDataValueConverter()
    private val fromDataEntryConverter = converterFactory.fromDataEntryConverter()
    private val nodeContractStateProvider = BlockingClientNodeContractStateValuesProvider(
        contractService = contractService
    )
    private val contractStateFactory = DefaultBackingMapContractStateFactory(
        nodeContractStateValuesProvider = nodeContractStateProvider,
        contractFromDataEntryConverter = fromDataEntryConverter,
        contractToDataValueConverter = toDataValueConverter,
    )
    private val paramsBuilder: ParamsBuilder = ParamsBuilderImpl(toDataValueConverter)
    private val txTypeResolver: TxTypeResolver = TxTypeResolverImpl()
    private val invocationHandlerFactory = ContractHandlerInvocationHandlerFactory(paramsBuilder, txTypeResolver)

    @Suppress("UNCHECKED_CAST")
    fun executeContract(txSigner: TxSigner, receiver: (S) -> Unit): ExecutionContext {
        var resultTx: ContractTx? = null
        val contractHandlerInvocationHandler =
            invocationHandlerFactory.handleContractInvocation { params: List<DataEntry>, txType: TxType ->
                val signRequest = contractSignRequestBuilder
                    .params(params)
                    .build(txType)
                val tx = txSigner.sign(signRequest)
                handleLocalContractValidation(contractStateFactory, fromDataEntryConverter, tx)
                resultTx = txService.broadcast(tx)
            }
        receiver(
            Proxy.newProxyInstance(
                contractInterface.classLoader,
                arrayOf(contractInterface),
                contractHandlerInvocationHandler,
            ) as S
        )
        return ExecutionContext(
            tx = requireNotNull(resultTx) {
                "Result tx is null. Probably you have passed an empty receiver."
            }
        )
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

data class ExecutionContext(
    val tx: ContractTx,
)
