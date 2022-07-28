package com.wavesenterprise.sdk.contract.client.invocation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam
import com.wavesenterprise.sdk.contract.api.state.ContractToDataValueConverter
import com.wavesenterprise.sdk.contract.client.invocation.impl.ParamsBuilderImpl
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
        methods: Pair<Method, Array<out Any>>,
    ) {
        val (method, args) = methods
        val params = paramsBuilder.build(method, args)
        val actionDataValue = firstDataEntry(method.name)

        assertEquals(actionDataValue, params[0])
        for (i in params.indices) {
            if (i == 0) {
                assertEquals(actionDataValue, params[i])
                continue
            }
            params[i].apply {
                assertEquals(
                    DataEntry(
                        key = DataKey(method.parameters[i - 1].getAnnotation(InvokeParam::class.java).name),
                        value = contractToDataValueConverter.convert(args[i - 1])
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

        @JvmStatic
        fun contractMethods(): Stream<Pair<Method, Array<out Any>?>> =
            Stream.of(
                TestProcessor::testActionInit.javaMethod!! to arrayOf(TestObject()),
                TestProcessor::testActionCallBoolean.javaMethod!! to arrayOf(true),
                TestProcessor::testActionCallInteger.javaMethod!! to arrayOf(1),
                TestProcessor::testActionCallString.javaMethod!! to arrayOf("test"),
                TestProcessor::testActionCallWithoutParams.javaMethod!! to arrayOf(),
                TestProcessor::testActionCallWithSeveralParameters.javaMethod!! to arrayOf(TestObject(), 1, "str", true)
            )
    }
}
