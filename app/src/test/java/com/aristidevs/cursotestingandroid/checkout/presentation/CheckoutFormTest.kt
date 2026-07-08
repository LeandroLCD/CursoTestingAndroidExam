package com.aristidevs.cursotestingandroid.checkout.presentation

import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS de la validación del formulario de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutForm.validate], [CheckoutFormErrors.isValid], [FieldError].
 */
class CheckoutFormTest {

    @Test
    fun `given blank name when validate then nameError is REQUIRED`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given blank address when validate then addressError is REQUIRED`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given blank email when validate then emailError is REQUIRED`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given malformed email when validate then emailError is INVALID_EMAIL`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given all fields valid when validate then errors isValid is true`() {
        // GIVEN

        // WHEN

        // THEN
    }
}
