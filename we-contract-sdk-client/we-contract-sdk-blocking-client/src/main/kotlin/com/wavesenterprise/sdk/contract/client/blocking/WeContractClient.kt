package com.wavesenterprise.sdk.contract.client.blocking

import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.sign.SignRequest
import com.wavesenterprise.sdk.node.domain.tx.ExecutableTx
import java.lang.reflect.Proxy

//
// interface ContractClientFactory<T> {
//    createContractClient()
// }

class ContractClientBuilder<T>() {
    private var enableLocalContractValidation: Boolean = false
    private var localContractValidator: LocalContractValidator? = null
    private var contractHandlerClass: Class<T>? = null

    fun contractHandlerClass(contractHandlerClass: Class<T>) = this.apply {
        this.contractHandlerClass = contractHandlerClass
    }
}

interface LocalContractValidator

interface TransactionBroadcaster {
    fun <T : ExecutableTx> broadcast(tx: T): T
}

interface TransactionSigner {
    fun <T : ExecutableTx> sign(request: SignRequest<T>): T
}

@Suppress("UNCHECKED_CAST")
class ContractClientContractInvocationFactory<T>(
    private val contractHandlerClass: Class<T>,
) {

    fun contractInvocation(contract: T, contractInvocation: (T) -> Unit) {
        val contractHandler: T = buildContractHandler()
        contractInvocation(contractHandler)
    }

    private fun buildContractHandler(): T = Proxy.newProxyInstance(
        contractHandlerClass.classLoader, arrayOf<Class<*>>(contractHandlerClass)
    ) { proxy, method, args ->
        val action = method.name
        Any()
    } as T
}

sealed interface ContractInvocation {
    class CreateContractInvocation(
        val params: List<DataEntry>,
    ) : ContractInvocation

    class CallContractInvocation(
        val params: List<DataEntry>,
    ) : ContractInvocation
}

//
