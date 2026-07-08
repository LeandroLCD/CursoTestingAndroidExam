package com.aristidevs.cursotestingandroid.checkout.presentation

data class CheckoutForm(
    val name:String = "",
    val address:String = "",
    val email:String = ""
)

enum class FieldError {REQUIRED, INVALID_EMAIL}
data class CheckoutFormErrors(
    val nameError:FieldError? = null,
    val addressError:FieldError? = null,
    val emailError:FieldError? = null
){
    val isValid: Boolean get() = nameError == null && addressError == null && emailError == null
}

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun CheckoutForm.validate(): CheckoutFormErrors = CheckoutFormErrors(
    nameError = if (name.isBlank()) FieldError.REQUIRED else null,
    addressError = if(address.isBlank()) FieldError.REQUIRED else null,
    emailError = when{
        email.isBlank() -> FieldError.REQUIRED
        !EMAIL_REGEX.matches(email) -> FieldError.INVALID_EMAIL
        else -> null
    }
)
