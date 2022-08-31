package com.wavesenterprise.sdk.contract.client.invocation.factory

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.client.invocation.MyContractHandler
import com.wavesenterprise.sdk.contract.client.invocation.MyContractHandlerImpl
import com.wavesenterprise.sdk.contract.client.invocation.util.callContractTx
import com.wavesenterprise.sdk.contract.client.invocation.util.createContractTx
import com.wavesenterprise.sdk.contract.client.invocation.util.user
import com.wavesenterprise.sdk.contract.jackson.JacksonConverterFactory
import com.wavesenterprise.sdk.contract.test.Util.randomBytesFromUUID
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.Fee
import com.wavesenterprise.sdk.node.domain.FeeAssetId
import com.wavesenterprise.sdk.node.domain.Hash
import com.wavesenterprise.sdk.node.domain.Password
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.TxVersion
import com.wavesenterprise.sdk.node.domain.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.blocking.node.NodeBlockingServiceFactory
import com.wavesenterprise.sdk.node.domain.blocking.tx.TxService
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractImage
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.sign.SignRequest
import com.wavesenterprise.sdk.node.domain.sign.builder.ContractSignRequestBuilder
import com.wavesenterprise.sdk.node.domain.tx.CallContractTx
import com.wavesenterprise.sdk.node.domain.tx.CreateContractTx
import com.wavesenterprise.sdk.tx.signer.TxSigner
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
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
        val contractSignRequestBuilder = ContractSignRequestBuilder()
            .version(txVersion)
            .senderAddress(senderAddress)
            .password(password)
            .fee(fee)
            .feeAssetId(feeAssetId)
            .contractVersion(contractVersion)
            .contractId(contractId)

        val contractBlockingClientFactory = ContractBlockingClientFactory(
            contractClass = contractClass,
            contractInterface = contractInterface,
            txSigner = txSigner,
            converterFactory = converterFactory,
            contractClientProperties = contractClientParams,
            contractSignRequestBuilder = contractSignRequestBuilder,
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
        )
        val contract = contractBlockingClientFactory.createContractClient()

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

        contract.put(user)

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

        val contractSignRequestBuilder = ContractSignRequestBuilder()
            .version(txVersion)
            .senderAddress(senderAddress)
            .password(password)
            .fee(fee)
            .feeAssetId(feeAssetId)
            .image(image)
            .imageHash(imageHash)
            .contractName(contractName)

        val contractBlockingClientFactory = ContractBlockingClientFactory(
            contractClass = contractClass,
            contractInterface = contractInterface,
            txSigner = txSigner,
            converterFactory = converterFactory,
            contractClientProperties = contractClientParams,
            contractSignRequestBuilder = contractSignRequestBuilder,
            nodeBlockingServiceFactory = nodeBlockingServiceFactory,
        )
        val contract = contractBlockingClientFactory.createContractClient()

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

        contract.createContract(user)
        println()

        verify { txSigner.sign(signRequestCapture.captured) }
        verify { txService.broadcast(createContractTx) }
    }

    companion object {
        val txVersion = TxVersion(1)
        val senderAddress = Address(randomBytesFromUUID())
        val password = Password("password")
        val fee = Fee(0L)
        val feeAssetId = FeeAssetId.fromTxId(TxId(randomBytesFromUUID()))
        val image = ContractImage("image")
        val imageHash = Hash(randomBytesFromUUID())
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
