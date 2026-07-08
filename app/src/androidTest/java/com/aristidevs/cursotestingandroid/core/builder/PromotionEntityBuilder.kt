package com.aristidevs.cursotestingandroid.core.builder

import com.aristidevs.cursotestingandroid.productlist.data.local.database.entity.PromotionEntity

class PromotionEntityBuilder {
    private var id: String = "promotion-1"
    private var type: String = "PERCENT"
    private var productIds: String = """["productId1"]"""
    private var value: Double = 10.0
    private var buyQuantity: Int? = null

    private var startAtEpoch: Long = 1700000000L
    private var endAtEpoch: Long = 1800000000L

    private var buyX: Int? = null
    private var payY: Int? = null
    private var percent: Int? = null

    fun withId(id: String) = apply { this.id = id }

    fun withType(type: String) = apply { this.type = type }

    fun withProductIds(productIds: String) = apply { this.productIds = productIds }

    fun withValue(value: Double) = apply { this.value = value }

    fun withBuyQuantity(buyQuantity: Int?) = apply { this.buyQuantity = buyQuantity }

    fun withStartTime(startTime: Long) = apply { this.startAtEpoch = startTime }

    fun withEndTime(endTime: Long) = apply { this.endAtEpoch = endTime }

    fun withPayY(payY: Int?) = apply { this.payY = payY }

    fun withBuyX(buyX: Int?) = apply { this.buyX = buyX }

    fun withPercent(percent: Int?) = apply { this.percent = percent }

    fun build() =
        PromotionEntity(
            id = id,
            type = type,
            productIds = productIds,
            startAtEpoch = startAtEpoch,
            endAtEpoch = endAtEpoch,
            buyX = buyX,
            payY = payY,
            percent = percent,
        )
}

fun promotionEntity(block: PromotionEntityBuilder.() -> Unit = {}) = PromotionEntityBuilder().apply(block).build()
