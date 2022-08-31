package com.wavesenterprise.sdk.contract.core.converter.factory

import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter

interface ConverterFactory {

    fun toDataValueConverter(): ContractToDataValueConverter

    fun fromDataEntryConverter(): ContractFromDataEntryConverter
}
