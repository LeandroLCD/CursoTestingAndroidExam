package com.aristidevs.cursotestingandroid.checkout.presentation

import android.content.Context
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.annotation.IdRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.aristidevs.cursotestingandroid.R
import com.aristidevs.cursotestingandroid.core.mothers.uistate.CheckoutUiStateModer
import com.aristidevs.cursotestingandroid.core.presentation.testing.UiTestTag
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    val appContext: Context? = ApplicationProvider.getApplicationContext<Context>()

    val resources: Resources? = appContext?.resources

    fun idResources(
        @IdRes id: Int,
    ) = resources?.getResourceEntryName(
        id,
    ) ?: throw IllegalStateException(
        "Resources not found",
    )

    fun idResources(
        @IdRes id: Int,
        vararg formatArgs: Any?,
        separator: Char = '_',
    ): String {
        val tag = resources?.getResourceEntryName(id) ?: throw IllegalStateException("Resources not found")
        val tagWithArgs = tag.plus(separator).plus(
            formatArgs.joinToString(
                separator = separator.toString(),
            ),
        )
        return tagWithArgs
    }

    fun renderScreen(
        uiState: CheckoutUiState = CheckoutUiState.Loading,
        onBack: () -> Unit = {},
        onRetry: () -> Unit = {},
        onNameChange: (String) -> Unit = {},
        onEmailChange: (String) -> Unit = {},
        onAddressChange: (String) -> Unit = {},
        onConfirm: () -> Unit = {},
    ) {
        composeRule.setContent {
            CheckoutContent(
                uiState = uiState,
                onBack = onBack,
                onRetry = onRetry,
                onNameChange = onNameChange,
                onEmailChange = onEmailChange,
                onAddressChange = onAddressChange,
                onConfirm = onConfirm,
            )
        }
    }

    @Test
    fun givenLoadingState_whenRendered_thenShowsProgress() {
        // GIVEN
        val state = CheckoutUiState.Loading

        // WHEN
        renderScreen(state)

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.progress_checkout)).assertExists()
        composeRule.onNodeWithTag(idResources(R.id.progress_checkout)).assertIsDisplayed()
    }

    @Test
    fun givenIdleStateWithEmptyCart_whenRendered_thenConfirmButtonDisabled() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutIdle(isCartEmpty = true, canSubmit = false)

        // WHEN
        renderScreen(state)

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsNotEnabled()
        composeRule.onNodeWithText("Tu carrito está vacío").assertIsDisplayed()
    }

    @Test
    fun givenIdleStateWithValidForm_whenRendered_thenConfirmButtonEnabled() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutIdle()

        // WHEN
        renderScreen(state)

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsEnabled()
    }

    @Test
    fun givenIdleState_whenTypingInvalidEmail_thenConfirmButtonDisabled() {
        // GIVEN
        val form = CheckoutUiStateModer.checkoutFormDefault.copy(email = "invalid-email")
        val state = CheckoutUiStateModer.checkoutIdle(
            form = form,
            errors = CheckoutUiStateModer.checkoutFormErrorsDefault.copy(emailError = FieldError.INVALID_EMAIL),
            canSubmit = false,
        )

        // WHEN
        renderScreen(state)

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.email_field)).assertTextContains(form.email)
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsNotEnabled()
    }

    @Test
    fun givenSuccessState_whenRendered_thenShowsOrderConfirmation() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutSuccess()

        // WHEN
        renderScreen(state)

        // THEN
        composeRule.onNodeWithText("Pedido confirmado: ${state.confirmation.orderId}").assertIsDisplayed()
        composeRule.onNodeWithText("Tiempo estimado: ${state.confirmation.etaMinutes}").assertIsDisplayed()
        composeRule.onNodeWithText("Precio: ${state.confirmation.total}").assertIsDisplayed()
    }

    @Test
    fun givenErrorState_whenRetryClicked_thenInvokesRetryCallback() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutError()
        var isRetryClicked = false

        // WHEN
        renderScreen(state, onRetry = {
            isRetryClicked = true
        })

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.button_retry)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.button_retry)).performClick()
        assertTrue(isRetryClicked, "Retry callback should be invoked when retry button is clicked")
    }

    @Test
    fun givenIdleState_whenConfirmClicked_thenInvokesConfirmCallback() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutIdle()
        var isConfirmClicked = false

        // WHEN
        renderScreen(state, onConfirm = {
            isConfirmClicked = true
        })

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).assertIsDisplayed()
        composeRule.onNodeWithTag(idResources(R.id.button_confirm)).performClick()
        assertTrue(isConfirmClicked, "Confirm callback should be invoked when confirm button is clicked")
    }

    @Test
    fun givenIdleState_whenTypingEmail_thenInvokesEmailChangeCallback() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutIdle(form = CheckoutUiStateModer.checkoutFormDefault.copy(email = ""))
        val typedEmail = "new_email@test.com"
        var receivedEmail: String? = null

        // WHEN
        renderScreen(state, onEmailChange = {
            receivedEmail = it
        })

        // THEN
        composeRule.onNodeWithTag(idResources(R.id.email_field)).performTextInput(typedEmail)
        assertEquals(typedEmail, receivedEmail, "EmailChange callback should receive the typed email")
    }

    @Test
    fun givenIdleState_whenBackPressed_thenInvokesBackCallback() {
        // GIVEN
        val state = CheckoutUiStateModer.checkoutIdle()
        var isBackClicked = false

        // WHEN
        renderScreen(state, onBack = {
            isBackClicked = true
        })

        // THEN
        composeRule.onNodeWithTag(UiTestTag.TOP_APP_BAR).assertIsDisplayed()
        composeRule.onNodeWithTag(UiTestTag.TOP_APP_BAR).performClick()
        assertTrue(isBackClicked, "Back callback should be invoked when top app bar is clicked")
    }
}
