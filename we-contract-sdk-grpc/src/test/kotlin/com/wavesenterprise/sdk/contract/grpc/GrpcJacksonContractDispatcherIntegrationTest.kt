package com.wavesenterprise.sdk.contract.grpc

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.contract.api.state.TypeReference
import com.wavesenterprise.sdk.contract.api.state.mapping.Mapping
import com.wavesenterprise.sdk.contract.grpc.connect.GrpcChannelProvider
import com.wavesenterprise.sdk.contract.grpc.connect.GrpcConnectionProperties
import com.wavesenterprise.sdk.node.client.blocking.contract.ContractService
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.TxType
import com.wavesenterprise.sdk.node.domain.contract.AuthToken
import com.wavesenterprise.sdk.node.domain.contract.CallContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.ContractId
import com.wavesenterprise.sdk.node.domain.contract.ContractName
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.ContractTransactionResponse
import com.wavesenterprise.sdk.node.domain.contract.ContractVersion
import com.wavesenterprise.sdk.node.domain.contract.CreateContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.ExecutionErrorRequest
import io.grpc.Channel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.netty.util.concurrent.ImmediateExecutor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class GrpcJacksonContractDispatcherIntegrationTest {

    @MockK
    lateinit var connectContractService: ContractService

    @MockK
    lateinit var txContractService: ContractService

    @MockK
    lateinit var connectionProperties: GrpcConnectionProperties

    @MockK
    lateinit var contractTransactionResponse: ContractTransactionResponse

    @Test
    fun `should pass integration test for create`() {
        mockIncomingTransaction(
            CreateContractTransaction(
                id = TxId.fromBase58("2nfSLahtZMk8wjD5fiPtfYiNYDKmyNpgSvB8bRgPSrQU"),
                type = TxType.CREATE_CONTRACT,
                sender = mockk(),
                senderPublicKey = mockk(),
                contractId = ContractId.fromBase58("2nfSLahtZMk8wjD5fiPtfYiNYDKmyNpgSvB8bRgPSrQU"),
                params = listOf(
                    DataEntry(
                        key = DataKey("action"),
                        value = DataValue.StringDataValue("createContract")
                    ),
                    DataEntry(
                        key = DataKey("createContract"),
                        value = DataValue.StringDataValue("initialValue")
                    ),
                ),
                fee = mockk(),
                version = mockk(),
                proof = mockk(),
                timestamp = mockk(),
                feeAssetId = mockk(),
                image = mockk(),
                imageHash = mockk(),
                contractName = ContractName("int test")
            )
        )

        val dispatcher = GrpcJacksonContractDispatcherBuilder.builder()
            .contractHandlerType(IntTestContractHandler::class.java)
            .connectContractService(connectContractService)
            .txContractService(txContractService)
            .channelProvider(mockChannelProvider())
            .objectMapper(jacksonObjectMapper())
            .executor(ImmediateExecutor.INSTANCE)
            .grpcConnectionProperties(stubConnecitonProperties())
            .build()

        dispatcher.dispatch()
    }

    @Test
    fun `should pass integration test for action`() {
        mockIncomingTransaction(
            CallContractTransaction(
                id = TxId.fromBase58("2nfSLahtZMk8wjD5fiPtfYiNYDKmyNpgSvB8bRgPSrQU"),
                type = TxType.CALL_CONTRACT,
                sender = mockk(),
                senderPublicKey = mockk(),
                contractId = ContractId.fromBase58("2nfSLahtZMk8wjD5fiPtfYiNYDKmyNpgSvB8bRgPSrQU"),
                params = listOf(
                    DataEntry(
                        key = DataKey("action"),
                        value = DataValue.StringDataValue("doSomeAction")
                    ),
                    DataEntry(
                        key = DataKey("createContract"),
                        value = DataValue.StringDataValue("initialValue")
                    ),
                ),
                fee = mockk(),
                version = mockk(),
                proof = mockk(),
                timestamp = mockk<Timestamp>().also { every { it.utcTimestampMillis } returns 1L },
                feeAssetId = mockk(),
                contractVersion = ContractVersion(1)
            )
        )
        every {
            txContractService.getContractKey(any())
        } returns Optional.empty()

        val dispatcher = GrpcJacksonContractDispatcherBuilder.builder()
            .contractHandlerType(IntTestContractHandler::class.java)
            .connectContractService(connectContractService)
            .txContractService(txContractService)
            .channelProvider(mockChannelProvider())
            .objectMapper(jacksonObjectMapper().registerModule(JavaTimeModule()))
            .executor(ImmediateExecutor.INSTANCE)
            .grpcConnectionProperties(stubConnecitonProperties())
            .build()

        dispatcher.dispatch()
    }

    private fun mockIncomingTransaction(contractTransaction: ContractTransaction) {
        every {
            contractTransactionResponse.transaction
        } returns contractTransaction
        every {
            contractTransactionResponse.authToken
        } returns AuthToken("bla")
        every {
            connectContractService.connect(any())
        } returns sequenceOf(contractTransactionResponse)
        every { txContractService.commitExecutionSuccess(any()) } returns Unit
        every { txContractService.commitExecutionError(any()) } answers {
            firstArg<ExecutionErrorRequest>().run {
                throw Exception("$code - $message")
            }
        }
    }

    private fun mockChannelProvider() = object : GrpcChannelProvider {
        override fun getChannel(grpcConnectionProperties: GrpcConnectionProperties): Channel = mockk()
    }

    private fun stubConnecitonProperties() = object : GrpcConnectionProperties {
        override val nodeHost: String = ""
        override val nodePort: Int = 0
        override val connectionId: String = ""
        override val authToken: String = ""
        override val keepAliveSeconds: Long = 0L
    }
}

@ContractHandler
class IntTestContractHandler(
    private val contractState: ContractState,
    private val tx: ContractTransaction,
) {

    private val mapping: Mapping<List<MySampleContractDto>> =
        contractState.getMapping(object : TypeReference<List<MySampleContractDto>>() {}, "SOME_PREFIX")

    @ContractInit
    fun createContract(initialParam: String) {
        contractState.put("INITIAL_PARAM", initialParam)
    }

    @ContractAction
    fun doSomeAction(dtoId: String) {
        contractState.put("INITIAL_PARAM", Instant.ofEpochMilli(tx.timestamp.utcTimestampMillis))
        require(!mapping.has(dtoId)) { "Already has $dtoId on state" }
        mapping.put(
            dtoId,
            listOf(
                MySampleContractDto("john", 18),
                MySampleContractDto("harry", 54)
            )
        )
    }

    @ContractAction
    @Throws(Exception::class)
    fun doActionWithErrorLogic(initialParam: String) {
        val utcTimestampMillis: Long = tx.timestamp.utcTimestampMillis
        if (utcTimestampMillis % 2 == 0L) {
            throw Exception("Tx timestamp is $utcTimestampMillis which is odd. Throwing Error")
            // throwing checked exeception for retry
        }
        contractState.put("success-$utcTimestampMillis", "hooray")
    }
}

data class MySampleContractDto(val name: String, val age: Int)
