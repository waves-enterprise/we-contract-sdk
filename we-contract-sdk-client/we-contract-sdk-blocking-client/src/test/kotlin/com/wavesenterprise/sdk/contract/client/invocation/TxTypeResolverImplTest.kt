package com.wavesenterprise.sdk.contract.client.invocation

import com.wavesenterprise.sdk.contract.client.invocation.impl.TxTypeResolverImpl
import com.wavesenterprise.sdk.node.domain.TxType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.Method
import java.util.stream.Stream
import kotlin.reflect.jvm.javaMethod

class TxTypeResolverImplTest {
    private val txTypeResolverImpl = TxTypeResolverImpl()

    @ParameterizedTest
    @MethodSource("contractMethods")
    fun `should resolve transaction type`(
        method: Pair<Method, TxType>,
    ) {
        val txType = txTypeResolverImpl.resolve(method.first)

        assertEquals(method.second, txType)
    }

    companion object {
        @JvmStatic
        fun contractMethods(): Stream<Pair<Method, TxType>> =
            Stream.of(
                TestProcessor::testActionInit.javaMethod!! to TxType.CREATE_CONTRACT,
                TestProcessor::testActionCallWithoutParams.javaMethod!! to TxType.CALL_CONTRACT,
                TestProcessor::testActionCallBoolean.javaMethod!! to TxType.CALL_CONTRACT,
            )
    }
}
