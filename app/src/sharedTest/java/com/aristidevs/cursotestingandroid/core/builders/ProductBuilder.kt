package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.productlist.domain.model.Product

class ProductBuilder {
    private var id: String = "product-1"
    private var name: String = "Producto pruebas"
    private var description = "Descripción completa"
    private var price: Double = 10.0
    private var category: String = "Test category"
    private var stock: Int = 10
    private var imageUrl: String? = null

    fun withId(id: String) = apply { this.id = id }

    fun withName(name: String) = apply { this.name = name }

    fun withDescription(description: String) = apply { this.description = description }

    fun withPrice(price: Double) = apply { this.price = price }

    fun withCategory(category: String) = apply { this.category = category }

    fun withStock(stock: Int) = apply { this.stock = stock }

    fun withImageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }

    fun build() =
        Product(
            id = id,
            name = name,
            description = description,
            price = price,
            category = category,
            stock = stock,
            imageUrl = imageUrl,
        )
}

fun product(block: ProductBuilder.() -> Unit = {}) = ProductBuilder().apply(block).build()
