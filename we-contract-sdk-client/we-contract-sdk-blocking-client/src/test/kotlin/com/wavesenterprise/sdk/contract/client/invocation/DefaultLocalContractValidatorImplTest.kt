package com.wavesenterprise.sdk.contract.client.invocation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.client.local.validator.impl.DefaultLocalContractValidatorImpl
import com.wavesenterprise.sdk.contract.api.exception.ContractPreValidationException
import com.wavesenterprise.sdk.contract.api.state.NodeContractStateValuesProvider
import com.wavesenterprise.sdk.contract.client.invocation.util.callContractTransaction
import com.wavesenterprise.sdk.contract.client.invocation.util.user
import com.wavesenterprise.sdk.contract.core.state.factory.DefaultBackingMapContractStateFactory
import com.wavesenterprise.sdk.contract.jackson.JacksonContractToDataValueConverter
import com.wavesenterprise.sdk.contract.jackson.JacksonFromDataEntryConverter
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.reflect.Proxy
import java.util.Optional
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class DefaultLocalContractValidatorImplTest {

    @MockK
    lateinit var mockParamsBuilder: ParamsBuilder

    @MockK
    lateinit var mockTxTypeResolver: TxTypeResolver

    @MockK
    lateinit var nodeContractStateValuesProvider: NodeContractStateValuesProvider

    private val contractFromDataEntryConverter = JacksonFromDataEntryConverter(jacksonObjectMapper())
    private val contractToDataValueConverter = JacksonContractToDataValueConverter(jacksonObjectMapper())

    lateinit var defaultLocalContractValidatorImpl: DefaultLocalContractValidatorImpl

    @BeforeEach
    fun init() {
        defaultLocalContractValidatorImpl = DefaultLocalContractValidatorImpl(
            contractHandlerType = MyContractHandlerImpl::class.java,
            contractStateFactory = DefaultBackingMapContractStateFactory(
                nodeContractStateValuesProvider = nodeContractStateValuesProvider,
                contractFromDataEntryConverter = contractFromDataEntryConverter,
                contractToDataValueConverter = contractToDataValueConverter,
            ),
            contractFromDataEntryConverter = contractFromDataEntryConverter,
        )
    }

    @Test
    fun `should throw prevalidation exception on second call`() {
        val id = UUID.randomUUID()
        val user = user(id = id)
        val userDataEntry = DataEntry(
            key = DataKey("user"),
            value = contractToDataValueConverter.convert(user),
        )

        val invocationHandler = ContractHandlerInvocationHandler(
            paramsBuilder = mockParamsBuilder,
            txTypeResolver = mockTxTypeResolver,
        ) { params, _ ->
            val contractTransaction: ContractTransaction = callContractTransaction(params)
            defaultLocalContractValidatorImpl.validate(contractTransaction)
        }
        val contractHandler: MyContractHandler = Proxy.newProxyInstance(
            MyContractHandler::class.java.classLoader,
            arrayOf(MyContractHandler::class.java),
            invocationHandler,
        ) as MyContractHandler

        every { mockParamsBuilder.build(any(), any()) } returns
            listOf(
                DataEntry(
                    key = DataKey("action"),
                    value = DataValue.StringDataValue("put"),
                ),
                userDataEntry,
            )
        every { mockTxTypeResolver.resolve(any()) } returns TxType.CALL_CONTRACT
        every {
            nodeContractStateValuesProvider.getForKey(any(), any())
        } returnsMany listOf(
            Optional.empty(),
            Optional.of(userDataEntry)
        )

        assertDoesNotThrow { contractHandler.put(user) }
        assertThrows<ContractPreValidationException> { contractHandler.put(user) }
    }
}
