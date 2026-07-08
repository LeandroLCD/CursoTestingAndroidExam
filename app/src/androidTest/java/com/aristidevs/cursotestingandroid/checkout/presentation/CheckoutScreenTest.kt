package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * EXAMEN — Tests de UI (Compose) de la pantalla de checkout.
 *
 * Completa cada test siguiendo Given-When-Then. No modifiques producción.
 * SUT: composables de [CheckoutScreen] / CheckoutContent renderizando cada [CheckoutUiState].
 * Pistas: usa `composeRule.setContent { ... }` pasando el estado deseado y callbacks de prueba;
 * localiza nodos por texto (la pantalla aún no expone testTags) y verifica habilitación del botón.
 */
class CheckoutScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgress() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenIdleStateWithEmptyCart_whenRendered_thenConfirmButtonDisabled() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenIdleStateWithValidForm_whenRendered_thenConfirmButtonEnabled() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenIdleState_whenTypingInvalidEmail_thenConfirmButtonDisabled() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsOrderConfirmation() {
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenInvokesRetryCallback() {
        // GIVEN

        // WHEN

        // THEN
    }
}
