package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction

interface ContractHandlerFactory<T> {
    fun createHandler(tx: ContractTransaction): T
}
