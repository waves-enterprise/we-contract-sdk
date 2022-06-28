package com.wavesenterprise.sdk.contract.api.domain

enum class ContractConcurrency {

    /**
     * Default value for IO intensive contracts
     * (contracts that reads many values from Blockchain)
     */
    IO,

    /**
     * Default value for CPU intensive contract
     * (contracts that contain complex cryptography for example)
     */
    CPU,

    /**
     * Default value for single-threaded contracts
     */
    OFF
}

private const val IO_ASYNC_MULTIPLIER = 4

fun ContractConcurrency.toThreadCount(processorCount: Int) =
    when (this) {
        ContractConcurrency.IO -> processorCount * IO_ASYNC_MULTIPLIER
        ContractConcurrency.CPU -> processorCount
        ContractConcurrency.OFF -> 1
    }
