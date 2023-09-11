package com.wavesenterprise.sdk.contract.client.invocation.factory

import com.wavesenterprise.sdk.client.local.validator.LocalContractValidation
import com.wavesenterprise.sdk.client.local.validator.impl.DefaultLocalContractValidatorImpl
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.client.invocation.ParamsBuilder
import com.wavesenterprise.sdk.contract.client.invocation.TxTypeResolver
import com.wavesenterprise.sdk.contract.client.invocation.impl.ParamsBuilderImpl
import com.wavesenterprise.sdk.contract.client.invocation.impl.TxTypeResolverImpl
import com.wavesenterprise.sdk.contract.core.converter.factory.ConverterFactory
import com.wavesenterprise.sdk.contract.core.node.BlockingClientNodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.core.state.LocalValidationContextManager
import com.wavesenterprise.sdk.contract.core.state.factory.DefaultBackingMapContractStateFactory
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.TxFeature
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.TxVersionDictionary
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.converter.toContractTransaction
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilderFactory
import com.wavesenterprise.sdk.node.domain.tx.ContractTx
import com.wavesenterprise.sdk.tx.signer.TxSigner
import java.lang.reflect.Proxy

class ContractBlockingClientFactory<S>(
    private val contractClass: Class<out S>?,
    private val contractInterface: Class<S>,
    private val localContractValidation: LocalContractValidation,
    private val contractSignRequestBuilderFactory: ContractSignRequestBuilderFactory,
    private val txSigner: TxSigner? = null,
    private val converterFactory: ConverterFactory,
    private val nodeBlockingServiceFactory: NodeBlockingServiceFactory,
    private val localValidationContextManager: LocalValidationContextManager,
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

    fun executeContract(contractId: ContractId? = null, txSigner: TxSigner, receiver: (S) -> Unit): ExecutionContext {
        return executionContext(
            contractId = contractId,
            txSigner = txSigner,
            receiver = receiver,
        )
    }

    fun executeContract(contractId: ContractId? = null, receiver: (S) -> Unit): ExecutionContext {
        requireNotNull(txSigner) {
            "TxSigner can not be null"
        }
        return executionContext(
            contractId = contractId,
            txSigner = txSigner,
            receiver = receiver,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun executionContext(
        contractId: ContractId? = null,
        txSigner: TxSigner,
        receiver: (S) -> Unit,
    ): ExecutionContext {
        var resultTx: ContractTx? = null
        val contractHandlerInvocationHandler =
            invocationHandlerFactory.handleContractInvocation { params: List<DataEntry>, txType: TxType ->
                val signRequestBuilder = contractSignRequestBuilderFactory.create(contractId)
                    .params(params)
                contractId?.let {
                    signRequestBuilder.contractId(it)
                }
                signRequestBuilder.version(TxVersion(TxVersionDictionary.maxVersionSupports(txType, TxFeature.ATOMIC)))
                val tx = txSigner.sign(signRequestBuilder.build(txType))
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
        fromDataEntryConverter: ContractFromDataEntryConverter,
        tx: ContractTx
    ) {
        if (!localContractValidation.isEnabled()) {
            return
        }
        val contractTransaction = tx.toContractTransaction()
        contractClass?.also {
            DefaultLocalContractValidatorImpl(
                contractHandlerType = contractClass,
                contractStateFactory = contractStateFactory,
                contractFromDataEntryConverter = fromDataEntryConverter,
                localValidationContextManager = localValidationContextManager,
            ).validate(contractTransaction)
        } ?: throw IllegalArgumentException("Contract implementation is required for local validation")
    }
}

data class ExecutionContext(
    val tx: ContractTx,
)
