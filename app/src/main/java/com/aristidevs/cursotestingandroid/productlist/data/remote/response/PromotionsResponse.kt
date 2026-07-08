package com.aristidevs.cursotestingandroid.productlist.data.remote.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromotionsResponse(
    @SerialName("promotions")
    val promotions: List<PromotionResponse>,
)

@Serializable
data class PromotionResponse(
    @SerialName("id")
    val id: String,
    @SerialName("productId")
    val productId: String,
    @SerialName("type")
    val type: String,
    @SerialName("percent")
    val percent: Int? = null,
    @SerialName("buyX")
    val buyX: Int? = null,
    @SerialName("payY")
    val payY: Int? = null,
    @SerialName("startAtEpoch")
    val startAtEpoch: Long? = null,
    @SerialName("endAtEpoch")
    val endAtEpoch: Long? = null,
)
