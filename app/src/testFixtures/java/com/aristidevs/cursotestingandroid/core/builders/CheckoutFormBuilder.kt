package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.checkout.presentation.CheckoutForm

class CheckoutFormBuilder {
    private var name: String = "John Doe"
    private var address: String = "123 Main St"
    private var email: String = "john@example.com"

    fun withName(name: String) = apply { this.name = name }

    fun withAddress(address: String) = apply { this.address = address }

    fun withEmail(email: String) = apply { this.email = email }

    fun build() =
        CheckoutForm(
            name = name,
            address = address,
            email = email,
        )
}

fun checkoutForm(block: CheckoutFormBuilder.() -> Unit = {}) = CheckoutFormBuilder().apply(block).build()
