# Módulo 1 — Crear la app (base)

**Rama:** `modulo1/proyecto` (== `master`)
**Objetivo:** construir la app *Mini Market* que servirá como sistema bajo test en los módulos siguientes. Aún **no escribimos tests** — sólo sentamos las bases.

---

## Stack técnico

| Capa | Librería | Versión |
|---|---|---|
| Lenguaje | Kotlin | 2.3.0 |
| Build | AGP | 9.0.0 |
| JDK | Java | 17 |
| Min / Target / Compile SDK | — | 26 / 36 / 36 |
| UI | Jetpack Compose (BOM) | 2026.01.01 |
| Material | Material3 + icons-extended | — |
| Navegación | Navigation 3 | 1.0.0 |
| DI | Hilt + KSP | 2.59 |
| BD local | Room | 2.8.4 |
| Red | Retrofit 3 + OkHttp 5 + kotlinx-serialization | 3.0.0 / 5.3.2 |
| Preferencias | DataStore Preferences | — |
| Asíncrono | Coroutines + Flow | 1.10.2 |
| Imágenes | Coil 3 | — |

Catálogo de versiones: `gradle/libs.versions.toml`.

---

## Arquitectura

Módulo único `app` con organización **feature-first** y capas `data` / `domain` / `presentation`:

```
com.aristidevs.cursotestingandroid/
├── MarketApp.kt              @HiltAndroidApp
├── MainActivity.kt           @AndroidEntryPoint, lanza NavGraph()
├── core/                     transversal: tema, NavGraph, Room DB, Retrofit, DataStore, Clock, DispatchersProvider
├── productlist/              feature: listado de productos
│   ├── data/                 ProductRepositoryImpl, ProductDao, PromotionDao, remote/local data sources, mappers
│   ├── domain/               GetProductsUseCase, GetPromotionForProduct, interfaces de repositorios
│   └── presentation/         ProductListViewModel, ProductListScreen, UiState, events
├── detail/                   feature: detalle de producto + promociones
├── cart/                     feature: carrito (AddToCart, UpdateCartItem, GetCartSummary, etc.)
├── settings/                 feature: tema + filtros (DataStore)
└── di/                       DataModule, NetworkModule
```

### Puntos de entrada

- `MarketApp` — `@HiltAndroidApp`.
- `MainActivity` — `@AndroidEntryPoint`. Observa `MainViewModel` para el tema y dibuja `NavGraph()`.
- `NavGraph.kt` — usa **Navigation 3** (no Navigation 2 con NavController).

### Abstracciones clave (importantes para módulos posteriores)

- `Clock` / `SystemClock` — fuente de tiempo inyectable → en tests se reemplaza por `FakeSystemClock`.
- `DispatchersProvider` / `DefaultDispatchersProvider` — dispatchers inyectables.
- `AppError` — modelo sellado de errores (`NetworkError`, `NotFoundError`, `Validation.*`).
- `MiniMarketDatabase` (Room) con `ProductDao`, `PromotionDao`, `CartItemDao`.

---

## Convenciones que aplicaremos al testear

1. **UiState como `sealed`/`data class`** por pantalla (`ProductListUiState.Success`, `CartUiState`, etc.). Lo emitimos por `StateFlow`.
2. **Events one-shot** vía `Channel` / `SharedFlow` para mensajes que NO son estado.
3. **Use cases como `class operator fun invoke(...)`** — fáciles de instanciar en tests.
4. **Repositorios devuelven `Flow`** de Room. Para refrescar desde la red, métodos `refresh*()` separados.
5. **`ViewModel` recibe sus dependencias por constructor (Hilt)** — sin singletons globales.

---

## Comandos Gradle relevantes para este módulo

```bash
./gradlew assembleDebug        # compilar
./gradlew installDebug         # instalar en emulador/dispositivo
./gradlew lint                 # análisis estático
```

---

## Lo que sigue

En el **Módulo 2** añadiremos JUnit, MockK, Turbine y `kotlinx-coroutines-test` y comenzaremos a testear use cases y ViewModels con **fakes + builders**.
