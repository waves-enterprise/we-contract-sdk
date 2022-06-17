package com.wavesenterprise.sdk.contract.grpc.connect

import com.wavesenterprise.sdk.contract.grpc.connect.auth.AuthTokenSupplier
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor

class HeaderClientInterceptor(
    private val authTokenSupplier: AuthTokenSupplier,
) : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        return object : SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                val authToken = authTokenSupplier.get()
                // log.debug("put auth header to request. Header: {} token: {}", AUTHORIZATION_HEADER_KEY, authToken)
                headers.put(AUTHORIZATION_HEADER_KEY, authToken.value)
                super.start(
                    object : SimpleForwardingClientCallListener<RespT>(responseListener) {
                        override fun onHeaders(headers: Metadata) {
                            super.onHeaders(headers)
                        }
                    },
                    headers
                )
            }
        }
    }

    companion object {
        //        private val log: Logger = LoggerFactory.getLogger(HeaderClientInterceptor::class.java)
        val AUTHORIZATION_HEADER_KEY: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    }
}
