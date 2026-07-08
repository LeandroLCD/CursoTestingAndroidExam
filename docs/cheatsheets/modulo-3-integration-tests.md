# Módulo 3 — Integration Tests

**Rama:** `modulo3/integration`
**Objetivo:** testear **Room real**, **Retrofit real contra MockWebServer**, y **ViewModels integrados** con Hilt. Tests instrumentados (corren en emulador/dispositivo).

---

## Dependencias añadidas

`libs.versions.toml`:

```toml
mockwebserver = "5.3.2"

hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }
mockwebserver        = { group = "com.squareup.okhttp3", name = "mockwebserver", ... }
kotlin-test          = { group = "org.jetbrains.kotlin", name = "kotlin-test", version.ref = "kotlin" }
```

`app/build.gradle.kts`:

```kotlin
androidTestImplementation(libs.turbine)
androidTestImplementation(libs.mockwebserver)
androidTestImplementation(libs.hilt.android.testing)
androidTestImplementation(libs.kotlin.test)
testImplementation(libs.mockwebserver)
```

---

## Configuración del módulo (lo nuevo)

### 1. `sharedTest` source set

`MainDispatcherRule` se movió a `app/src/sharedTest/java/...` y se enlaza tanto a `test` como a `androidTest`. Así reutilizamos la misma regla en unit + instrumentado:

```kotlin
android {
    sourceSets {
        getByName("test")        { java.directories.add("src/sharedTest/java") }
        getByName("androidTest") { java.directories.add("src/sharedTest/java") }
    }
}
```

### 2. `HiltTestRunner` (runner personalizado)

Sustituye `Application` por `HiltTestApplication`. Sin esto, los tests con `@HiltAndroidTest` no inyectan.

```kotlin
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application? =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
```

Y en `defaultConfig`:

```kotlin
testInstrumentationRunner = "com.aristidevs.cursotestingandroid.HiltTestRunner"
```

### 3. `AndroidManifest` específico de `debug`

`app/src/debug/AndroidManifest.xml` añade la activity vacía que Hilt necesita para tests.

---

## Tres tipos de test instrumentado en este módulo

### A) **Tests de DAO (Room en memoria)**

Sin Hilt. Construimos la BD en memoria, ejercitamos el DAO, validamos con Turbine.

```kotlin
@RunWith(AndroidJUnit4::class)
class ProductDaoTest {
    private lateinit var database: MiniMarketDatabase
    private lateinit var dao: ProductDao

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MiniMarketDatabase::class.java
        ).build()
        dao = database.productDao()
    }

    @After fun tearDown() { database.close() }

    @Test fun givenInsertedProduct_whenGetProductById_thenReturnsProduct() = runTest {
        val p = productEntity { withId("id") }
        dao.insertProducts(listOf(p))
        assertEquals("id", dao.getProductById("id").first().id)
    }
}
```

Builders de entity en `app/src/androidTest/java/.../core/builder/`: `productEntity { }`, `cartItemEntity { }`, `promotionEntity { }`.

### B) **Tests de Repository con Hilt + MockWebServer**

Inyectamos el repositorio real, pero sustituimos `NetworkModule` por `TestNetworkModule` que apunta a un MockWebServer.

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {

    @get:Rule(order = 0) val mockWebServer = MockWebServerRule()
    @get:Rule(order = 1) val hilt = HiltAndroidRule(this)

    @Inject lateinit var productRepository: ProductRepository

    @Before fun setUp() { hilt.inject() }

    @Test fun givenValidJson_whenRefresh_thenRoomEmitsProducts() = runTest {
        mockWebServer.server.enqueue(MockResponse().setBody(productsJson).setResponseCode(200))
        productRepository.refreshProduct()
        val products = productRepository.getProducts().first()
        assertEquals(2, products.size)
    }
}
```

⚠️ **Orden de reglas importa**: `MockWebServerRule` (0) arranca primero → escribe `MockWebServerUrlHolder.baseUrl` → `HiltAndroidRule` (1) construye el grafo con ese baseUrl.

### C) **Tests de ViewModel integrado**

ViewModel real + Hilt + MockWebServer + DataStore real. Tres reglas en orden:

```kotlin
@get:Rule(order = 0) val mockWebServer    = MockWebServerRule()
@get:Rule(order = 1) val hilt             = HiltAndroidRule(this)
@get:Rule(order = 2) val mainDispatcherRule = MainDispatcherRule()
```

---

## Piezas reutilizables nuevas

### `MockWebServerRule`

```kotlin
class MockWebServerRule : TestWatcher() {
    val server = MockWebServer()
    override fun starting(description: Description?) {
        server.start()
        MockWebServerUrlHolder.baseUrl = server.url("/").toString()
    }
    override fun finished(description: Description?) { server.shutdown() }
}
```

### `MockWebServerUrlHolder` — singleton que comunica la URL viva del server al `TestNetworkModule` antes de que Hilt construya Retrofit.

### `MiniMarketApiDispatcher` — dispatcher de rutas (sirve JSON real desde `assets/`):

```kotlin
class MiniMarketApiDispatcher(
    private val productJson: String,
    private val promoJson: String = """{"promotions":[]}"""
) : Dispatcher() {
    override fun dispatch(request: RecordedRequest) = when {
        request.path?.contains("promotions.json") == true ->
            MockResponse().setBody(promoJson).setResponseCode(200)
        request.path?.contains("products.json") == true ->
            MockResponse().setBody(productJson).setResponseCode(200)
        else -> MockResponse().setResponseCode(404)
    }
}
```

### `ProductErrorDispatcher` — simula 500 en `/products.json` para tests offline-first.

### `TestNetworkModule` — reemplaza el `NetworkModule` real con uno apuntando a `MockWebServerUrlHolder.baseUrl`:

```kotlin
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule { /* OkHttp, Json, Retrofit con baseUrl del MockWebServer */ }
```

### `TestDataModule` — reemplaza `DataModule` real (DataStore en archivo `testing_settings`, Room real, etc.).

### Helpers

```kotlin
// JsonUtils.kt
fun String.asAsset(): String = JsonUtils.readJson(this)
// uso:
"product_list_default.json".asAsset()
```

```kotlin
// TurbineUtils.kt — para flujos que emiten múltiples estados antes del que esperamos
suspend fun <T> ReceiveTurbine<T>.awaitStateMatching(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
```

### Assets de test

`app/src/androidTest/assets/`:
- `product_list_default.json`
- `product_list_updated.json`
- `promotions_buy_x_pay_y.json`
- `promotions_percent.json`

---

## Test estrella del módulo: `OfflineFirstIntegrationTest`

Verifica el contrato offline-first del `ProductRepositoryImpl`:

1. ✅ Refresh OK → Room contiene los productos remotos.
2. ✅ Caché vacía + refresh falla (500) → emite lista vacía y lanza `AppError.NetworkError`.
3. ✅ Caché con datos + refresh falla → mantiene la caché previa.

Patrón: alternar el `dispatcher` de MockWebServer entre `MiniMarketApiDispatcher` y `ProductErrorDispatcher` dentro del mismo test.

---

## Reglas de oro del módulo

1. **Cada test instrumentado parte de estado limpio.** `database.clearAllTables()` en `@Before`, `(settingsRepository as? SettingsRepositoryImpl)?.clear()` para DataStore.
2. **MockWebServerRule antes que HiltAndroidRule** (rule order 0 < 1).
3. **No mocks aquí.** Si quieres mockear es un unit test, no integración.
4. **JSON real desde assets**, no strings inline gigantes — facilita versionar y reusar fixtures.
5. **Para flakiness con Flows**, usa `awaitStateMatching { predicate }` en lugar de `awaitItem()` ciego.

---

## Comandos

```bash
# Todos los tests instrumentados (requiere emulador encendido)
./gradlew connectedDebugAndroidTest

# Una clase
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
com.aristidevs.cursotestingandroid.core.OfflineFirstIntegrationTest

# Un método
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
com.aristidevs.cursotestingandroid.productlist.data.repository.ProductRepositoryImplTest#givenValidProductsJson_whenRefreshIsCalled_thenDatabaseEmitProductsFromRoom
```
