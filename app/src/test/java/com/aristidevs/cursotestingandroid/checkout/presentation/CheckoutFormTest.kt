package com.aristidevs.cursotestingandroid.checkout.presentation

import com.aristidevs.cursotestingandroid.core.builders.checkoutForm
import com.aristidevs.cursotestingandroid.core.builders.checkoutFormErrors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        val form = checkoutForm {
            withName("   ")
        }
        val expected = checkoutFormErrors {
            withNameError(FieldError.REQUIRED)
        }

        // WHEN
        val result = form.validate()

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `given blank address when validate then addressError is REQUIRED`() {
        // GIVEN
        val form = checkoutForm {
            withAddress("")
        }
        val expected = checkoutFormErrors {
            withAddressError(FieldError.REQUIRED)
        }

        // WHEN
        val result = form.validate()

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `given blank email when validate then emailError is REQUIRED`() {
        // GIVEN
        val form = checkoutForm {
            withEmail("")
        }
        val expected = checkoutFormErrors {
            withEmailError(FieldError.REQUIRED)
        }

        // WHEN
        val result = form.validate()

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `given malformed email when validate then emailError is INVALID_EMAIL`() {
        // GIVEN
        val form = checkoutForm {
            withEmail("email@email")
        }
        val expected = checkoutFormErrors {
            withEmailError(FieldError.INVALID_EMAIL)
        }

        // WHEN
        val result = form.validate()

        // THEN
        assertEquals(expected, result)
    }

    @Test
    fun `given all fields valid when validate then errors isValid is true`() {
        // GIVEN
        val form = checkoutForm{
            withName("John Doe")
            withAddress("123 Main St")
            withEmail("john@aristidev.cl")
        }

        // WHEN
        val result = form.validate()

        // THEN
        assertTrue(result.isValid)
    }
}
