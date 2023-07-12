package com.wavesenterprise.sdk.contract.client.invocation.factory

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.client.invocation.MyContractHandler
import com.wavesenterprise.sdk.contract.client.invocation.MyContractHandlerImpl
import com.wavesenterprise.sdk.contract.client.invocation.util.callContractTx
import com.wavesenterprise.sdk.contract.client.invocation.util.createContractTx
import com.wavesenterprise.sdk.contract.client.invocation.util.user
import com.wavesenterprise.sdk.contract.core.state.LocalValidationContextManager
import com.wavesenterprise.sdk.contract.core.state.ThreadLocalLocalValidationContextManager
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import com.wavesenterprise.sdk.contract.test.Util.randomBytesFromUUID
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.client.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.client.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.FeeAssetId
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractImageHash
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.SignRequest
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilderFactory
import com.wavesenterprise.sdk.node.domain.tx.CallContractTx
import com.wavesenterprise.sdk.node.domain.tx.CreateContractTx
import com.wavesenterprise.sdk.tx.signer.TxSigner
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Optional
import java.util.UUID
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class ContractBlockingClientFactoryTest {

    private val contractClass = MyContractHandlerImpl::class.java

    private val contractInterface = MyContractHandler::class.java

    private val objectMapper = jacksonObjectMapper()

    private val localValidationContextManager: LocalValidationContextManager = ThreadLocalLocalValidationContextManager()

    @MockK
    lateinit var txSigner: TxSigner

    @MockK
    lateinit var nodeBlockingServiceFactory: NodeBlockingServiceFactory

    @MockK
    lateinit var contractService: ContractService

    @MockK
    lateinit var txService: TxService

    lateinit var converterFactory: JacksonConverterFactory

    @BeforeEach
    fun init() {
        converterFactory = JacksonConverterFactory(objectMapper)

        every { nodeBlockingServiceFactory.txService() } returns txService
        every { nodeBlockingServiceFactory.contractService() } returns contractService
    }

    @ParameterizedTest
    @MethodSource("contractClientParams")
    fun `should create client and broadcast callContractTx`(
        contractClientParams: ContractClientParams,
    ) {
        val user = user(id = UUID.randomUUID())
        val userDataEntry = DataEntry(
            key = DataKey("user"),
            value = converterFactory.toDataValueConverter().convert(user),
        )
        val contractSignRequestBuilderFactory = object : ContractSignRequestBuilderFactory {
            override fun create(): ContractSignRequestBuilder = ContractSignRequestBuilder()
                .version(txVersion)
                .fee(fee)
                .feeAssetId(feeAssetId)
                .contractVersion(contractVersion)
                .contractId(contractId)
        }
        val contractBlockingClientFactory = ContractBlockingClientFactory(
            contractClass = contractClass,
            contractInterface = contractInterface,
            converterFactory = converterFactory,
            contractClientProperties = contractClientParams,
            contractSignRequestBuilderFactory = contractSignRequestBuilderFactory,
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
            localValidationContextManager = localValidationContextManager,
        )
        val signRequestCapture = slot<SignRequest<CallContractTx>>()
        val txCaptor = slot<CallContractTx>()
        val callContractTx = callContractTx(
            listOf(
                DataEntry(DataKey("action"), DataValue.StringDataValue("put")),
                userDataEntry,
            )
        )
        every { txSigner.sign(capture(signRequestCapture)) } returns callContractTx
        every { txService.broadcast(capture(txCaptor)) } returns callContractTx
        every { contractService.getContractKey(any()) } returns Optional.empty()

        contractBlockingClientFactory.executeContract(txSigner = txSigner) { contract ->
            contract.put(user)
        }

        verify { txSigner.sign(signRequestCapture.captured) }
        verify { txService.broadcast(callContractTx) }
    }

    @ParameterizedTest
    @MethodSource("contractClientParams")
    fun `should create client and broadcast createContractTx`(
        contractClientParams: ContractClientParams,
    ) {
        val user = user(id = UUID.randomUUID())
        val userDataEntry = DataEntry(
            key = DataKey("user"),
            value = converterFactory.toDataValueConverter().convert(user),
        )

        val contractSignRequestBuilderFactory = object : ContractSignRequestBuilderFactory {
            override fun create(): ContractSignRequestBuilder = ContractSignRequestBuilder()
                .version(txVersion)
                .fee(fee)
                .feeAssetId(feeAssetId)
                .image(image)
                .imageHash(imageHash)
                .contractName(contractName)
        }

        val contractBlockingClientFactory = ContractBlockingClientFactory(
            contractClass = contractClass,
            contractInterface = contractInterface,
            converterFactory = converterFactory,
            contractClientProperties = contractClientParams,
            contractSignRequestBuilderFactory = contractSignRequestBuilderFactory,
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
            localValidationContextManager = localValidationContextManager,
        )
        val signRequestCapture = slot<SignRequest<CreateContractTx>>()
        val txCaptor = slot<CreateContractTx>()
        val createContractTx = createContractTx(
            listOf(
                DataEntry(DataKey("action"), DataValue.StringDataValue("createContract")),
                userDataEntry,
            )
        )

        every { txSigner.sign(capture(signRequestCapture)) } returns createContractTx
        every { txService.broadcast(capture(txCaptor)) } returns createContractTx
        every { contractService.getContractKey(any()) } returns Optional.empty()

        contractBlockingClientFactory.executeContract(txSigner = txSigner) { contract ->
            contract.createContract(user)
        }

        verify { txSigner.sign(signRequestCapture.captured) }
        verify { txService.broadcast(createContractTx) }
    }

    @Test
    fun `should throw exception when txSigner is equals null`() {
        val contractBlockingClientFactory = ContractBlockingClientFactory(
            contractClass = contractClass,
            contractInterface = contractInterface,
            converterFactory = converterFactory,
            contractClientProperties = ContractClientParams(localValidationEnabled = true),
            contractSignRequestBuilderFactory = object : ContractSignRequestBuilderFactory {
                override fun create(): ContractSignRequestBuilder {
                    TODO("Not yet implemented")
                }
            },
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
            txSigner = null,
            localValidationContextManager = localValidationContextManager,
        )
        assertThrows<IllegalArgumentException> {
            contractBlockingClientFactory.executeContract { contract ->
                contract.createContract(user(id = UUID.randomUUID()))
            }
        }.apply {
            assertEquals("TxSigner can not be null", this.message)
        }
    }

    companion object {
        val txVersion = TxVersion(1)
        val fee = Fee(0L)
        val feeAssetId = FeeAssetId.fromTxId(TxId(randomBytesFromUUID()))
        val image = ContractImage("image")
        val imageHash = ContractImageHash("imageHash")
        val contractName = ContractName("contractName")
        val contractVersion = ContractVersion(1)
        val contractId = ContractId(TxId(randomBytesFromUUID()))

        @JvmStatic
        fun contractClientParams(): Stream<Arguments> = Stream.of(
            Arguments.of(ContractClientParams(localValidationEnabled = true)),
            Arguments.of(ContractClientParams(localValidationEnabled = false)),
        )
    }
}
