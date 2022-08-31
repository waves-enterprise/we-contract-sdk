package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.contract.api.state.ContractState
import java.util.UUID

@ContractHandler
interface MyContractHandler {

    @ContractInit
    fun createContract(@InvokeParam("user") user: User)

    @ContractAction
    fun put(@InvokeParam("user") user: User)
}

class MyContractHandlerImpl(
    private val contractState: ContractState,
) : MyContractHandler {

    private val users = contractState.getMapping(User::class.java, ID_KEY)

    override fun createContract(user: User) {
        users.put(user.id.toString(), user)
    }

    override fun put(user: User) {
        require(!users.tryGet(user.id.toString()).isPresent) {
            "ID already taken - ${user.id}"
        }
        users.put(user.id.toString(), user)
    }

    companion object {
        const val ID_KEY = "ID"
    }
}

data class User(
    val id: UUID,
    val name: String,
)
