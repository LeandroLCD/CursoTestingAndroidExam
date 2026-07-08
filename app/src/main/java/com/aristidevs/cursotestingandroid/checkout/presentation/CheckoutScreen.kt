package com.aristidevs.cursotestingandroid.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aristidevs.cursotestingandroid.core.presentation.components.MarketTopAppBar

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CheckoutEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    CheckoutContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = { viewModel.onRetry() },
        onNameChange = { viewModel.onNameChange(it) },
        onAddressChange = { viewModel.onAddressChange(it) },
        onEmailChange = { viewModel.onEmailChange(it) },
        onConfirm = { viewModel.onConfirm() },
    )
}

@Composable
fun CheckoutContent(
    uiState: CheckoutUiState,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { MarketTopAppBar(title = "Checkout") { onBack() } },
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (uiState) {
                CheckoutUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is CheckoutUiState.Error -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(uiState.message)
                        Button(onClick = { onRetry() }) {
                            Text("Reintentar")
                        }
                    }
                }

                is CheckoutUiState.Success -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("Pedido confirmado: ${uiState.confirmation.orderId}")
                        Text("Tiempo estimado: ${uiState.confirmation.etaMinutes}")
                        Text("Precio: ${uiState.confirmation.total}")
                    }
                }

                is CheckoutUiState.Idle -> {
                    CheckoutContentIdle(uiState, onNameChange, onEmailChange, onAddressChange, onConfirm)
                }
            }
        }
    }
}

@Composable
fun CheckoutContentIdle(
    uiState: CheckoutUiState.Idle,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Total: ${uiState.summary.finalTotal}", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.form.name,
            onValueChange = onNameChange,
            label = { Text("Nombre") },
            isError = uiState.errors.nameError != null,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.form.address,
            onValueChange = onAddressChange,
            label = { Text("Dirección") },
            isError = uiState.errors.addressError != null,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.form.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = uiState.errors.emailError != null,
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.isCartEmpty) {
            Text("Tu carrito está vacío")
        }

        Button(
            onClick = onConfirm,
            enabled = uiState.canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (uiState.isSubmitting) "Procesando el pago..." else "Confirmar pedido") }
    }
}
