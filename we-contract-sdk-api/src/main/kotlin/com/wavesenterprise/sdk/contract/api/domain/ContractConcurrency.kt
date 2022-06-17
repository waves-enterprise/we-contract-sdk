package com.wavesenterprise.sdk.contract.api.domain

enum class ContractConcurrency(
    val asyncMultiplier: Int,
) {

    /**
     * Default value for IO intensive contracts
     * (contracts that reads many values from Blockchain)
     */
    IO(4),

    /**
     * Default value for CPU intensive contract
     * (contracts that contain complex cryptography for example)
     */
    CPU(1),

    /**
     * Default value for single-threaded contracts
     */
    OFF(0)
}
