package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ContractHandlerInvocationHandler(
    private val paramsBuilder: ParamsBuilder,
    private val txTypeResolver: TxTypeResolver,
    private val localContractValidator: LocalContractValidator,
    val paramsReceiver: (List<DataEntry>, TxType) -> (Unit),
) : InvocationHandler {
    override fun invoke(
        proxy: Any,
        method: Method,
        args: Array<out Any>,
    ): Any {
        val params = paramsBuilder.build(method, args)
        val txType = txTypeResolver.resolve(method)
        val contractTransaction: ContractTransaction = buildContractTransaction(params, txType)
        localContractValidator.validate(contractTransaction)
        paramsReceiver(params, txType)
        return Any()
    }
}
