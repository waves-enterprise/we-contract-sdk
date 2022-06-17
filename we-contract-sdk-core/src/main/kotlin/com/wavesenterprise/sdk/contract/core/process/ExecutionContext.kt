package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction

interface ExecutionContext {
    val contractTransaction: ContractTransaction
}
