package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.contract.api.state.ContractFromDataEntryConverter
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.jvm.javaMethod

@ExtendWith(MockKExtension::class)
internal class ContractInvocationArgumentsExtractorImplTest {

    @MockK
    lateinit var contractFromDataEntryConverter: ContractFromDataEntryConverter

    @MockK
    lateinit var contractTx: ContractTransaction

    @Test
    fun `should extract converted params for method`() {
        val domainDto = SomeDomainDto("bla", 123)
        val longValue = 123L
        val stringValue = "stringVal"
        val booleanValue = false
        val domainArg = DataEntry(
            key = DataKey("someDomainDto"),
            value = DataValue.StringDataValue("bla-bla")
        )
        val longArg = DataEntry(
            key = DataKey("longParam"),
            value = DataValue.IntegerDataValue(1L)
        )
        val stringArg = DataEntry(
            key = DataKey("otherStringParamName"),
            value = DataValue.StringDataValue("abc")
        )
        val booleanArg = DataEntry(
            key = DataKey("boolParam"),
            value = DataValue.BooleanDataValue(booleanValue)
        )
        every {
            contractFromDataEntryConverter.convert(longArg, Long::class.java)
        } returns longValue
        every {
            contractFromDataEntryConverter.convert(stringArg, String::class.java)
        } returns stringValue
        every {
            contractFromDataEntryConverter.convert(any(), SomeDomainDto::class.java)
        } returns domainDto
        every {
            contractFromDataEntryConverter.convert(any(), Boolean::class.java)
        } returns booleanValue
        every {
            contractTx.params
        } returns listOf(
            stringArg,
            longArg,
            booleanArg,
            domainArg,
        )
        every {
            contractTx.id
        } returns TxId.fromBase58("2nfSLahtZMk8wjD5fiPtfYiNYDKmyNpgSvB8bRgPSrQU")

        val subj = ContractInvocationArgumentsExtractorImpl(contractFromDataEntryConverter)
        val invokeArgs = subj.extractArguments(SomeContractHandler::someContractAction.javaMethod!!, contractTx)

        invokeArgs.apply {
            assertEquals(4, size)
            assertEquals(get(0), stringValue)
            assertEquals(get(1), longValue)
            assertEquals(get(2), booleanValue)
            assertEquals(get(3), domainDto)
        }
    }
}

data class SomeDomainDto(
    val name: String,
    val age: Int,
)

@ContractHandler
class SomeContractHandler {

    @ContractAction
    fun someContractAction(
        @InvokeParam("otherStringParamName")
        stringParam: String,
        longParam: Long,
        boolParam: Boolean,
        someDomainDto: SomeDomainDto,
    ) {
    }
}
