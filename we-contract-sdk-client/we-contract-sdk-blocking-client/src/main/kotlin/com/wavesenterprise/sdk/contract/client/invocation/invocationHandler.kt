package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.TxType
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

// todo implement paramsBuilder and txTypeResolver

// example of usage

fun example() {
    val mockParamsBuilder = object : ParamsBuilder {
        override fun build(method: Method, args: Array<out Any>?): List<DataEntry> {
            TODO("Not yet implemented")
        }
    }
    val mockTxTypeResolver = object : TxTypeResolver {
        override fun resolve(method: Method): TxType {
            TODO("Not yet implemented")
        }
    }
    ContractHandlerInvocationHandler(
        paramsBuilder = mockParamsBuilder,
        txTypeResolver = mockTxTypeResolver
    ) { params, txType ->
        params.size
        txType.code
        // use params and txType to build sign request
    }
}

// api

class ContractHandlerInvocationHandlerFactory(
    val paramsBuilder: ParamsBuilder,
    val txTypeResolver: TxTypeResolver,
) {
    fun handleContractInvocation(receiver: (List<DataEntry>, TxType) -> (Unit)) =
        ContractHandlerInvocationHandler(paramsBuilder, txTypeResolver, receiver)
}

class ContractHandlerInvocationHandler(
    private val paramsBuilder: ParamsBuilder,
    private val txTypeResolver: TxTypeResolver,
    val paramsReceiver: (List<DataEntry>, TxType) -> (Unit),
) : InvocationHandler {
    override fun invoke(
        proxy: Any,
        method: Method,
        args: Array<out Any>?,
    ): Any {
        val params = paramsBuilder.build(method, args)
        val txType = txTypeResolver.resolve(method)
        paramsReceiver(params, txType)
        return Any()
    }
}

interface ParamsBuilder {
    fun build(method: Method, args: Array<out Any>?): List<DataEntry>
}

interface TxTypeResolver {
    fun resolve(method: Method): TxType
}
