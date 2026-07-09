package com.aristidevs.cursotestingandroid.core.builders

import com.aristidevs.cursotestingandroid.checkout.presentation.CheckoutFormErrors
import com.aristidevs.cursotestingandroid.checkout.presentation.FieldError

class CheckoutFormErrorsBuilder {
    private var nameError: FieldError? = null
    private var addressError: FieldError? = null
    private var emailError: FieldError? = null

    fun withNameError(nameError: FieldError?) = apply { this.nameError = nameError }

    fun withAddressError(addressError: FieldError?) = apply { this.addressError = addressError }

    fun withEmailError(emailError: FieldError?) = apply { this.emailError = emailError }

    fun build() =
        CheckoutFormErrors(
            nameError = nameError,
            addressError = addressError,
            emailError = emailError,
        )
}

fun checkoutFormErrors(block: CheckoutFormErrorsBuilder.() -> Unit = {}) = CheckoutFormErrorsBuilder()
    .apply(
        block,
    ).build()
