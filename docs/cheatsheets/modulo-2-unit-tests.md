# Módulo 2 — Unit Tests

**Rama:** `modulo2/unit_tests`
**Objetivo:** testear **use cases**, **ViewModels** y **extensiones puras** sin tocar Android ni red ni base de datos. Velocidad y aislamiento.

---

## Dependencias añadidas (`libs.versions.toml`)

```toml
mockk = "1.14.9"
turbine = "1.2.1"

kotlinx-coroutines-test = { ... version.ref = "coroutines" }
mockk                   = { group = "io.mockk", name = "mockk", ... }
turbine                 = { module = "app.cash.turbine:turbine", ... }
```

En `app/build.gradle.kts`:

```kotlin
testImplementation(libs.junit)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation(libs.mockk)
testImplementation(libs.turbine)
```

---

## Conceptos clave

### 1. Estructura Given–When–Then

Todos los tests siguen la misma estructura visible:

```kotlin
@Test
fun zero_quantity_throws_QuantityMustBePositive() = runTest {
    //Given
    val fakeCart = FakeCartItemRepository()
    val fakeProducts = FakeProductRepository()
    val useCase = AddToCartUseCase(fakeCart, fakeProducts)

    //When
    val exception = runCatching { useCase("id", 0) }.exceptionOrNull()

    //Then
    assertTrue(exception is AppError.Validation.QuantityMustBePositive)
}
```

### 2. `MainDispatcherRule` para coroutines

Reemplaza `Dispatchers.Main` por un `TestDispatcher` durante el test. Vive en `app/src/test/java/.../core/MainDispatcherRule.kt`:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(scheduler)
) : TestWatcher() {
    override fun starting(description: Description?)  { Dispatchers.setMain(testDispatcher) }
    override fun finished(description: Description?)  { Dispatchers.resetMain() }
}
```

Uso en tests de ViewModel:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun ... = runTest(mainDispatcherRule.scheduler) { ... }
```

### 3. **Fakes** sobre Mocks (preferencia del curso)

- `Fake*Repository` → implementación de prueba **en memoria** del interface real. Sirve para múltiples tests, ahorra mocking repetido.
- `FakeProductRepository.setProducts(...)`, `FakeCartItemRepository.setCartItems(...)`, etc.
- **MockK** se usa puntualmente para verificar interacciones (`coVerify`) o cuando montar un fake no aporta.

Ubicación: `app/src/test/java/.../core/fakes/`.

### 4. **Builders** para datos de test

En lugar de construir entidades a mano:

```kotlin
val product = product {        // ProductBuilder
    withId("id-1")
    withStock(10)
}

val item = cartItem {
    withProductId("id-1")
    withQuantity(3)
}
```

Cada builder expone una función de extensión DSL (`product { ... }`, `promotion { ... }`, `cartItem { ... }`).
Ubicación: `app/src/test/java/.../core/builders/`.

### 5. **Turbine** para flujos

```kotlin
viewModel.uiState.test {
    val state = awaitItem()
    assertTrue(state is ProductListUiState.Success)
    assertEquals(1, (state as Success).products.size)
    cancelAndIgnoreRemainingEvents()
}
```

### 6. **MockK** para verificar interacciones

Cuando lo que importa es *qué se llamó*:

```kotlin
val productRepository = mockk<ProductRepository>()
val cartRepository = mockk<CartItemRepository>()

coEvery { productRepository.getProductById(productId) } returns flowOf(product)
coEvery { cartRepository.getCartItemById(productId) }  returns null
coEvery { cartRepository.addToCart(productId, 3) }     just Runs

useCase(productId, 3)

coVerify(exactly = 1) { cartRepository.addToCart(productId, 3) }
```

Operadores: `coEvery`, `coVerify`, `just Runs`, `mockk<T>()`, `relaxed = true`.

### 7. **Stubs** específicos para casos extremos

Cuando un fake reutilizable es overkill (p.ej. simular un fallo): `FailingProductRepositoryStub.kt`.

### 8. `Clock` inyectable

Para no depender de `System.currentTimeMillis()` en tests de promociones con fecha:

```kotlin
val fakeClock = FakeSystemClock()   // configurable
```

---

## Mapa de tests del módulo

| Archivo | Qué prueba |
|---|---|
| `RoundTo2DecimalsTest` | Extensión pura de redondeo |
| `PromotionsExtensionsTest` | Lógica de promoción activa (con `Clock`) |
| `AddToCartUseCaseTest` | Validaciones + add real + verificación con MockK |
| `UpdateCartItemUseCaseTest` | Actualizar cantidad |
| `GetCartItemsWithPromotionsUseCaseTest` | Combinación cart × promotion |
| `GetCartSummaryUseCaseTest` | Totales y descuentos |
| `GetProductsUseCaseTest` | Filtrado + orden + settings |
| `GetPromotionForProductTest` | Match de promoción por producto |
| `GetProductDetailWithPromotionUseCaseTest` | Detalle con promo |
| `ProductListViewModelTest` | Estados con fakes |
| `ProductListViewModelMockTest` | Mismos casos con MockK (comparativa) |
| `CartViewModelTest`, `ProductDetailViewModelTest`, `SettingsViewModelTest`, `MainViewModelTest` | ViewModels |

---

## Reglas de oro del módulo

1. Un test, un comportamiento. Si necesitas más de un assert sobre cosas distintas, plantéate dividir.
2. Nombre de test descriptivo: `given_X_when_Y_then_Z`.
3. Si vas a crear un objeto en > 1 test, hazle un **builder**.
4. Si vas a stubear un repositorio en > 1 test, hazle un **fake**.
5. ViewModels: testea el `StateFlow` con **Turbine** + `MainDispatcherRule`, no `Thread.sleep`.

---

## Comandos

```bash
# Todos los unit tests
./gradlew testDebugUnitTest

# Un solo test
./gradlew :app:testDebugUnitTest \
  --tests "com.aristidevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCaseTest"

# Un solo método
./gradlew :app:testDebugUnitTest \
  --tests "com.aristidevs.cursotestingandroid.cart.domain.usecase.AddToCartUseCaseTest.zero_quantity_throws_QuantityMustBePositive"
```
