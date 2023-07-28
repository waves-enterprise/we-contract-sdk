package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.TxType
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

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
