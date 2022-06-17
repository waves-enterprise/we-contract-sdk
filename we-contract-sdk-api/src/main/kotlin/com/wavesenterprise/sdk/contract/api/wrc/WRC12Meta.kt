package com.wavesenterprise.sdk.contract.api.wrc

/**
 * Meta stored by a contract when first created
 */
data class WRC12Meta(
    val lang: String,
    val interfaces: List<String>,
    val impls: List<String>,
)
