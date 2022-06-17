package com.wavesenterprise.sdk.contract.grpc.connect.auth

import com.wavesenterprise.sdk.node.domain.contract.AuthToken

class ThreadLocalAuthTokenSupplier : AuthTokenSupplier {

    companion object {
        @JvmStatic
        private val authTokenThreadLocal: ThreadLocal<AuthToken> = ThreadLocal()

        @JvmStatic
        fun setToken(authToken: AuthToken) {
            authTokenThreadLocal.set(authToken)
        }

        @JvmStatic
        fun clearToken() {
            authTokenThreadLocal.remove()
        }
    }

    override fun get(): AuthToken = requireNotNull(authTokenThreadLocal.get())
}
