package com.wavesenterprise.sdk.contract.grpc.connect.auth

import com.wavesenterprise.sdk.node.domain.contract.AuthToken
import java.util.function.Supplier

interface AuthTokenSupplier : Supplier<AuthToken>
