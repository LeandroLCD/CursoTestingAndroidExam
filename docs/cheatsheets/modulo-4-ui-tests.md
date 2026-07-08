# Módulo 4 — UI Tests (Compose)

**Rama:** `modulo4/uitesting` *(en curso)*
**Objetivo:** testear pantallas **Compose** de forma aislada — sin Hilt, sin red, sin BD — verificando renderizado, interacciones y callbacks. El SUT es la pantalla, no el flujo completo.

---

## Dependencias

Ya estaban presentes desde el setup inicial, solo hay que asegurarse:

```kotlin
// app/build.gradle.kts (androidTest)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.compose.ui.test.junit4)
debugImplementation(libs.androidx.compose.ui.test.manifest)
```

> Los UI tests **corren en emulador o dispositivo** (son instrumentados).

---

## Patrón: separar `Screen` de `Content`

Para testear sin Hilt, separamos el composable **stateful** (que pide `hiltViewModel()`) del **stateless** (que recibe estado + callbacks):

```kotlin
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(                                  // ← esto es lo que testeamos
        uiState = uiState,
        onBack = onBack,
        onInStockOnlyChange = settingsViewModel::setInStockOnly,
        onThemeModeSelected = settingsViewModel::setThemeMode
    )
}

@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onInStockOnlyChange: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit
) { /* UI pura */ }
```

> El test sólo necesita `SettingsContent` + un `SettingsUiState` fabricado.

---

## `UiTestTag` — tags centralizados

En vez de strings sueltos por todo el código, un objeto único con constantes y helpers:

```kotlin
// app/src/main/java/.../core/presentation/testing/UiTestTag.kt
object UiTestTag {
    const val TOP_APP_BAR = "top_app_bar"

    // SETTINGS
    const val SETTINGS_CONTENT          = "settings_content"
    const val SETTINGS_IN_STOCK_SWITCH  = "settings_in_stock_switch"
    const val SETTINGS_TAX_SWITCH       = "settings_tax_switch"

    // Tag dinámico (uno por opción de tema)
    fun settingsThemeOption(themeName: String) = "settings_theme_${themeName.lowercase()}"
}
```

Aplicado en producción:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .testTag(SETTINGS_CONTENT)
        ...
)

Switch(
    modifier = Modifier.testTag(SETTINGS_IN_STOCK_SWITCH),
    ...
)
```

**Regla**: si un test necesita un nodo, primero añade la constante a `UiTestTag` y aplica `Modifier.testTag(...)` — nunca hardcodear strings en el test.

---

## Setup mínimo del test

```kotlin
class SettingsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun createSettingsScreen(
        uiState: SettingsUiState = SettingsUiState(),
        onBack: () -> Unit = {},
        onThemeModeSelected: (ThemeMode) -> Unit = {},
        onInStockOnlyChange: (Boolean) -> Unit = {},
    ) {
        composeRule.setContent {
            SettingsContent(uiState, onBack, onInStockOnlyChange, onThemeModeSelected)
        }
    }

    private fun getString(resId: Int): String = composeRule.activity.getString(resId)
}
```

Notas:
- `createAndroidComposeRule<ComponentActivity>()` — usa una `ComponentActivity` vacía, sin tu `MainActivity`.
- `createComposeRule()` también existe pero no da acceso a `activity` (no podrás resolver `R.string`).
- El helper `createSettingsScreen(...)` con defaults reduce ruido en cada test.

---

## Cheatsheet de API de testing Compose

### Buscar nodos

| API | Cuándo usar |
|---|---|
| `onNodeWithTag("...")` | Lo más estable — recomendado |
| `onNodeWithText("...")` | Para texto visible (probable rotura con i18n) |
| `onNodeWithContentDescription("...")` | Accesibilidad |
| `onAllNodesWith*` | Cuando hay varios |

### Aserciones

| API | Verifica |
|---|---|
| `.assertIsDisplayed()` | El nodo está en la composición y visible |
| `.assertExists()` | El nodo existe (no necesariamente visible) |
| `.assertDoesNotExist()` | No está |
| `.assertIsOn()` / `.assertIsOff()` | Switch / Checkbox |
| `.assertIsSelected()` / `.assertIsNotSelected()` | RadioButton, SegmentedButton |
| `.assertIsEnabled()` / `.assertIsNotEnabled()` | Habilitación |
| `.assertTextEquals("...")` / `.assertTextContains("...")` | Texto del nodo |

### Acciones

| API | Acción |
|---|---|
| `.performClick()` | Click |
| `.performTextInput("...")` | Escribir en TextField |
| `.performTextClearance()` | Vaciar TextField |
| `.performScrollTo()` | Scroll hasta el nodo |
| `.performImeAction()` | Acción del teclado (Done, Next...) |

### Sincronización

- Compose **sincroniza solo** con la composición — no hacen falta `IdlingResource`s para la mayoría de casos.
- Si necesitas esperar a algo: `composeRule.waitUntil(timeoutMillis = 5_000) { condition }`.
- Para tiempo virtual: `composeRule.mainClock.advanceTimeBy(...)`.

---

## Ejemplos del módulo

### 1. Verificar render por defecto

```kotlin
@Test
fun givenDefaultSettingsState_whenRendered_thenShowsFilterAndAppearanceSections() {
    createSettingsScreen(uiState = SettingsUiState())

    composeRule.onNodeWithText(getString(R.string.settings_title)).assertIsDisplayed()
    composeRule.onNodeWithTag(SETTINGS_CONTENT).assertIsDisplayed()
    composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).assertIsOff()
    composeRule.onNodeWithTag(SETTINGS_TAX_SWITCH).assertIsOn()
}
```

### 2. Verificar estado dependiente del input

```kotlin
@Test
fun givenLightTheme_whenRendered_thenLightOptionIsSelected() {
    createSettingsScreen(uiState = SettingsUiState(themeMode = ThemeMode.LIGHT))
    composeRule.onNodeWithTag(UiTestTag.settingsThemeOption("light")).assertIsSelected()
}
```

### 3. Verificar que un click emite el callback correcto

```kotlin
@Test
fun givenInStockSwitchOff_whenClicked_thenEmitsTrue() {
    var emitted: Boolean? = null

    createSettingsScreen(
        uiState = SettingsUiState(inStockOnly = false),
        onInStockOnlyChange = { newState -> emitted = newState }
    )

    composeRule.onNodeWithTag(SETTINGS_IN_STOCK_SWITCH).performClick()

    assertEquals(true, emitted)
}
```

### 4. Click sobre un tag dinámico

```kotlin
@Test
fun givenLightTheme_whenDarkClicked_thenEmitsDarkTheme() {
    var selectedTheme: ThemeMode? = null
    createSettingsScreen(
        uiState = SettingsUiState(themeMode = ThemeMode.LIGHT),
        onThemeModeSelected = { selectedTheme = it }
    )

    composeRule.onNodeWithTag(settingsThemeOption("dark")).performClick()

    assertEquals(ThemeMode.DARK, selectedTheme)
}
```

---

## Reglas de oro del módulo

1. **Testea el `Content` (stateless)**, no el `Screen` (stateful). El `Screen` queda probado por las pruebas de ViewModel del Módulo 2.
2. **Test tags centralizados** en `UiTestTag`. Cero strings sueltos en los tests.
3. **Captura los callbacks en variables locales** (`var emitted: T? = null`) y aserta sobre ellas — es la forma más simple de verificar interacciones.
4. **Nada de `Thread.sleep`**. Compose tiene auto-sync; si necesitas esperar usa `waitUntil`.
5. **Un test, una interacción.** No encadenes 5 clicks en un solo test.
6. **`createAndroidComposeRule<ComponentActivity>()`** salvo que necesites tu Activity real (raro).

---

## Comandos

```bash
# Todos los UI tests (emulador encendido)
./gradlew connectedDebugAndroidTest

# Sólo la clase de Settings
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
com.aristidevs.cursotestingandroid.settings.presentation.SettingsScreenTest

# Un único método
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
com.aristidevs.cursotestingandroid.settings.presentation.SettingsScreenTest#givenInStockSwitchOff_whenClicked_thenEmitsTrue
```

---

## Lo que vendrá

- Tests de pantallas con **listas** (`onNodeWithTag(...).onChildren()`, `performScrollToIndex`).
- Tests con **navegación** (`createAndroidComposeRule` + `TestNavHostController` o navegación real).
- Tests **end-to-end** combinando lo del Módulo 3 (Hilt + MockWebServer) con UI Compose → app completa contra un servidor falso.
