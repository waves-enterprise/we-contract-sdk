package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.domain.ContractCall
import com.wavesenterprise.sdk.contract.api.domain.DefaultContractCall
import com.wavesenterprise.sdk.contract.api.state.ContractState
import com.wavesenterprise.sdk.node.domain.Address
import com.wavesenterprise.sdk.node.domain.Timestamp
import com.wavesenterprise.sdk.node.domain.TxId
import com.wavesenterprise.sdk.node.domain.contract.ContractTransaction
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class ContractHandlerFactoryImplTest {

    @MockK
    lateinit var contractState: ContractState

    @MockK
    lateinit var contractTransaction: ContractTransaction

    @BeforeEach
    fun initMocks() {
        val txSenderAddress = Address.fromBase58("3Ng3g5ZSpbZQZa2P87AUzWPpJAMPj1ssnVE")
        val contractTxId = TxId.fromBase58("4C4Kq91ujZ7uj87QgRVeGUCYkrsLoqRVpBw78QMuxw2e")
        val contractTxTimestamp = Timestamp.fromUtcTimestamp(Instant.now().toEpochMilli())
        every { contractTransaction.sender } returns txSenderAddress
        every { contractTransaction.id } returns contractTxId
        every { contractTransaction.timestamp } returns contractTxTimestamp
    }

    @Test
    fun `should create contract handler with ContractTransaction`() {
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState,
            TestContractHandlerForContractTransaction::class.java
        )

        val handlerForState = contractHandlerFactory.createHandler(contractTransaction)

        assertEquals(contractTransaction, handlerForState.contractTransaction)
    }

    @Test
    fun `should create contract handler with ContractState`() {
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState,
            TestContractHandlerForState::class.java
        )

        val handlerForState = contractHandlerFactory.createHandler(contractTransaction)

        assertEquals(contractState, handlerForState.contractState)
    }

    @Test
    fun `should create contract handler with ContractCall and ContractState`() {
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState,
            TestContractHandlerForContractCallAndState::class.java
        )

        val handlerForStateAndCall = contractHandlerFactory.createHandler(contractTransaction)

        assertEquals(contractState, handlerForStateAndCall.contractState)
        assertEquals(DefaultContractCall(contractTransaction), handlerForStateAndCall.contractCall)
    }

    @Test
    fun `should create contract handler with ContractTransaction and ContractState`() {
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState,
            TestContractHandlerForContractTransactionAndState::class.java
        )

        val handlerForTransactionAndCall = contractHandlerFactory.createHandler(contractTransaction)

        assertEquals(contractState, handlerForTransactionAndCall.contractState)
        assertEquals(contractTransaction, handlerForTransactionAndCall.contractTransaction)
    }

    @Test
    fun `should create contract handler with ContractCall`() {
        val contractHandlerFactory = ContractHandlerFactoryImpl(
            contractState,
            TestContractHandlerForContractCall::class.java
        )

        val handlerForCall = contractHandlerFactory.createHandler(contractTransaction)

        assertEquals(DefaultContractCall(contractTransaction), handlerForCall.contractCall)
    }

    @Test
    fun `should throw exception when having more than one constructor`() {
        val illegalArgumentException = assertThrows<IllegalArgumentException> {
            val contractHandlerFactory = ContractHandlerFactoryImpl(
                contractState,
                TestContractHandlerWithSecondConstructor::class.java
            )
            contractHandlerFactory.createHandler(contractTransaction)
        }
        illegalArgumentException.apply {
            assertEquals(
                "ContractHandler ${TestContractHandlerWithSecondConstructor::class.java.canonicalName} " +
                    "class should have exactly one public constructor",
                message
            )
        }
    }
}

@ContractHandler
class TestContractHandlerForContractTransaction(
    val contractTransaction: ContractTransaction,
) {
    private constructor() : this(mockk())
}

@ContractHandler
class TestContractHandlerForState(
    val contractState: ContractState,
)

@ContractHandler
class TestContractHandlerForContractCall(
    val contractCall: ContractCall,
)

@ContractHandler
class TestContractHandlerForContractCallAndState(
    val contractCall: ContractCall,
    val contractState: ContractState,
)

@ContractHandler
class TestContractHandlerForContractTransactionAndState(
    val contractTransaction: ContractTransaction,
    val contractState: ContractState,
)

@ContractHandler
class TestContractHandlerWithSecondConstructor(
    val contractCall: ContractCall,
) {
    constructor() : this(mockk())
}
