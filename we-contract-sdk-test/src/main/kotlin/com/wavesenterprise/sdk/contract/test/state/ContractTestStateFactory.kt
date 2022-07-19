package com.wavesenterprise.sdk.contract.test.state

import com.fasterxml.jackson.databind.ObjectMapper
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.core.state.DefaultBackingMapContractStateFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonContractToDataValueConverter
import com.wavesenterprise.sdk.contract.jackson.JacksonFromDataEntryConverter
import com.wavesenterprise.sdk.contract.test.NoOpNodeValuesProvider
import com.wavesenterprise.sdk.contract.test.data.TestDataProvider.Companion.txId
import com.wavesenterprise.sdk.node.domain.contract.ContractId

class ContractTestStateFactory private constructor() {

    companion object {

        @JvmStatic
        fun state(objectMapper: ObjectMapper = ObjectMapper()): ContractState =
            DefaultBackingMapContractStateFactory(
                NoOpNodeValuesProvider,
                contractToDataValueConverter = JacksonContractToDataValueConverter(objectMapper),
                contractFromDataEntryConverter = JacksonFromDataEntryConverter(objectMapper)
            ).buildContractState(ContractId(txId()))
    }
}
