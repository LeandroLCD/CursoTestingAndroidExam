package com.aristidevs.cursotestingandroid.checkout.presentation

import com.aristidevs.cursotestingandroid.core.MainDispatcherRule
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests UNITARIOS del ViewModel de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: [CheckoutViewModel] — estados [CheckoutUiState], `canSubmit`, `onConfirm`, eventos.
 * Pistas: usa Turbine sobre `uiState`/`event`, `runTest(mainDispatcherRule.scheduler)`,
 * fakes (FakeCartItemRepository, FakeProductRepository, FakePromotionRepository, FakeSystemClock)
 * y un fake de OrderRepository que tendrás que crear.
 */
class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `given empty cart when initialized then canSubmit is false`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given valid form and non empty cart when form completed then canSubmit is true`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given malformed email when email changed then emailError is INVALID_EMAIL and canSubmit is false`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given valid form when onConfirm succeeds then emits Success state`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given place order fails when onConfirm then emits Error state and ShowMessage event`() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun `given invalid form when onConfirm then does not place order`() {
        // GIVEN

        // WHEN

        // THEN
    }
}
