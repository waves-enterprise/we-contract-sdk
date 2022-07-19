package com.wavesenterprise.sdk.contract.api.state

import com.wavesenterprise.sdk.node.domain.contract.ContractId

interface ContractState : ContractStateReader, ContractStateWriter {

    fun external(contractId: ContractId): ContractStateReader
}
