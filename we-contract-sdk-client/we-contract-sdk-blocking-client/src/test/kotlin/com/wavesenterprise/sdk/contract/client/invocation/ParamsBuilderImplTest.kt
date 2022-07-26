package com.wavesenterprise.sdk.contract.client.invocation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.jackson.JacksonContractToDataValueConverter
import com.wavesenterprise.sdk.node.domain.DataEntry
import com.wavesenterprise.sdk.node.domain.DataKey
import com.wavesenterprise.sdk.node.domain.DataValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.Method
import java.util.stream.Stream
import kotlin.reflect.jvm.javaMethod

internal class ParamsBuilderImplTest {
    private val contractToDataValueConverter: ContractToDataValueConverter = JacksonContractToDataValueConverter(
        jacksonObjectMapper()
    )
    private val paramsBuilder = ParamsBuilderImpl(contractToDataValueConverter)

    @ParameterizedTest
    @MethodSource("contractMethods")
    fun `should take params`(
        method: Pair<Method, Array<out Any>?>,
    ) {
        val (methods, args) = method
        val params = paramsBuilder.build(method.first, method.second)
        val actionDataValue = firstDataEntry(method.first.name)

        assertEquals(actionDataValue, params[0])
        args?.let { arg ->
            assertEquals(arg.size + 1, params.size)
            params[1].apply {
                assertEquals(
                    DataEntry(
                        key = DataKey(INVOKE_PARAM),
                        value = contractToDataValueConverter.convert(args[0])
                    ),
                    this
                )
            }
        }
    }

    private fun firstDataEntry(actionName: String) = DataEntry(
        key = DataKey(ACTION_PARAM_KEY),
        value = DataValue.StringDataValue(actionName),
    )

    companion object {
        const val ACTION_PARAM_KEY = "action"
        const val INVOKE_PARAM = "request"

        @JvmStatic
        fun contractMethods(): Stream<Pair<Method, Array<out Any>?>> =
            Stream.of(
                TestProcessor::testActionInit.javaMethod!! to arrayOf(TestObject()),
                TestProcessor::testActionCallBoolean.javaMethod!! to arrayOf(true),
                TestProcessor::testActionCallInteger.javaMethod!! to arrayOf(1),
                TestProcessor::testActionCallString.javaMethod!! to arrayOf("test"),
                TestProcessor::testActionCallWithoutParams.javaMethod!! to null,
            )
    }
}
