package com.wavesenterprise.sdk.contract.core.process

import com.wavesenterprise.sdk.contract.api.annotation.ContractAction
import com.wavesenterprise.sdk.contract.api.annotation.ContractHandler
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import com.wavesenterprise.sdk.node.domain.contract.CallContractTransaction
import com.wavesenterprise.sdk.node.domain.contract.CreateContractTransaction
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.IllegalArgumentException
import kotlin.reflect.jvm.javaMethod

@ExtendWith(MockKExtension::class)
internal class ContractHandlerInvocationExtractorTest {

    @MockK
    lateinit var createContractTx: CreateContractTransaction

    @MockK
    lateinit var callContractTx: CallContractTransaction

    @Test
    fun `should extract init method for CreateContractTx`() {
        every {
            createContractTx.params
        } returns listOf(
            DataEntry(
                key = DataKey("action"),
                value = DataValue.StringDataValue("init")
            )
        )

        val invocationExtractor = ContractHandlerInvocationExtractor(TestContractHandler::class.java)
        val extractedMethod = invocationExtractor.extractMethod(createContractTx)
        assertEquals(TestContractHandler::init.javaMethod, extractedMethod)
    }

    @Test
    fun `should extract customNamed init method for CreateContractTx`() {
        every {
            createContractTx.params
        } returns listOf(
            DataEntry(
                key = DataKey("action"),
                value = DataValue.StringDataValue("someOtherInitName")
            )
        )

        val invocationExtractor = ContractHandlerInvocationExtractor(TestContractHandler::class.java)
        val extractedMethod = invocationExtractor.extractMethod(createContractTx)
        assertEquals(TestContractHandler::otherInit.javaMethod, extractedMethod)
    }

    @Test
    fun `should extract ContractAction method for CallContractTx`() {
        every {
            callContractTx.params
        } returns listOf(
            DataEntry(
                key = DataKey("action"),
                value = DataValue.StringDataValue("contractAction")
            ),
            DataEntry(
                key = DataKey("param"),
                value = DataValue.StringDataValue("blaValue")
            ),
        )

        val invocationExtractor = ContractHandlerInvocationExtractor(TestContractHandler::class.java)
        val extractedMethod = invocationExtractor.extractMethod(callContractTx)
        assertEquals(TestContractHandler::contractAction.javaMethod, extractedMethod)
    }

    @Test
    fun `should extract custom named ContractAction method for CallContractTx`() {
        every {
            callContractTx.params
        } returns listOf(
            DataEntry(
                key = DataKey("action"),
                value = DataValue.StringDataValue("someOtherActionName")
            ),
            DataEntry(
                key = DataKey("param"),
                value = DataValue.StringDataValue("blaValue")
            ),
        )

        val invocationExtractor = ContractHandlerInvocationExtractor(TestContractHandler::class.java)
        val extractedMethod = invocationExtractor.extractMethod(callContractTx)
        assertEquals(TestContractHandler::someMethod.javaMethod, extractedMethod)
    }

    @Test
    fun `should throw error if method is not annotated`() {
        val actionName = "someNotAnnotatedMethod"
        every {
            callContractTx.params
        } returns listOf(
            DataEntry(
                key = DataKey("action"),
                value = DataValue.StringDataValue(actionName)
            ),
        )
        val invocationExtractor = ContractHandlerInvocationExtractor(TestContractHandler::class.java)

        assertThrows<IllegalArgumentException> {
            invocationExtractor.extractMethod(callContractTx)
        }.apply {
            assertEquals(
                "ContractAction named $actionName hasn't been found in class " +
                    "com.wavesenterprise.sdk.contract.core.process.TestContractHandler",
                message
            )
        }
    }
}

@ContractHandler
class TestContractHandler {

    @ContractInit
    fun init() {
    }

    @ContractInit("someOtherInitName")
    fun otherInit() {
    }

    @ContractAction
    fun contractAction(param: String) {
    }

    @ContractAction("someOtherActionName")
    fun someMethod() {
    }

    fun someNotAnnotatedMethod() {
    }
}
