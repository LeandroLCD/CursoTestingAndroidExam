# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`CursoTestingAndroid` is the companion project for an Android testing course. The branch naming convention is `moduloN/<topic>` (current: `modulo4/uitesting`). The app itself is a small mini-market (product list, detail, cart, settings) used as the system-under-test — most changes are about *adding or improving tests*, not product features.

## Build & test commands

Uses Gradle Kotlin DSL with a version catalog at `gradle/libs.versions.toml`. AGP 9.0.0, Kotlin 2.3.0, JDK 17, min SDK 26, target/compile SDK 36.

- `./gradlew assembleDebug` — build the debug APK
- `./gradlew test` — all JVM unit tests
- `./gradlew testDebugUnitTest` — unit tests for the debug variant
- `./gradlew connectedDebugAndroidTest` — instrumentation tests on a connected device/emulator (requires running emulator)
- `./gradlew lint` — Android lint
- Run a single unit test class: `./gradlew :app:testDebugUnitTest --tests "com.aristidevs.cursotestingandroid.productlist.presentation.ProductListViewModelTest"`
- Run a single test method: append `.methodName` to the class FQN above
- Run a single instrumentation test: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.aristidevs.cursotestingandroid.settings.presentation.SettingsScreenTest`

Instrumentation tests run via the **custom `HiltTestRunner`** declared in [app/build.gradle.kts:32](app/build.gradle.kts:32) which swaps the `Application` for `HiltTestApplication`.

## Source set layout (non-obvious)

The `app` module declares a third source set, `src/sharedTest/java`, that is added to **both** `test` and `androidTest` ([app/build.gradle.kts:16-23](app/build.gradle.kts:16)). Put test utilities reused by JVM and instrumentation tests there (e.g. `MainDispatcherRule`). Do not duplicate them across `test/` and `androidTest/`.

## Architecture

Single-module app under `com.aristidevs.cursotestingandroid`, organized **feature-first** with `data` / `domain` / `presentation` inside each feature:

- `core/` — cross-cutting: Room `MiniMarketDatabase`, Retrofit/OkHttp config, DataStore preferences, `DispatchersProvider`, `Clock`/`SystemClock`, Compose theme, `NavGraph` (Navigation3), `core/presentation/testing/UiTestTag.kt` (test-tag constants)
- `productlist/` — list screen, `GetProductsUseCase`, `ProductRepositoryImpl` with remote + local data sources and mappers
- `detail/` — product detail, `GetProductDetailWithPromotionUseCase`
- `cart/` — cart, `CartItemRepositoryImpl`, use cases (add/update/getWithPromotions/summary)
- `settings/` — theme + filter preferences
- `di/` — Hilt modules (`DataModule`, `NetworkModule`)

Entry points: `MarketApp` (`@HiltAndroidApp`), `MainActivity` (`@AndroidEntryPoint`, hosts `NavGraph()`). Networking: Retrofit 3 + kotlinx.serialization + OkHttp 5. Persistence: Room 2.8 with DAOs (`ProductDao`, `PromotionDao`, `CartItemDao`). DI: Hilt 2.59 via KSP. UI: Compose BOM 2026.01.01, Material3, Navigation3.

## Testing conventions — follow these when adding tests

The codebase has a consistent style; new tests should match it rather than inventing new patterns.

**Unit tests (`src/test/`)**
- Prefer hand-written **fakes** over mocks (see `core/fakes/Fake*Repository.kt`). Mockk is available but used sparingly (e.g. `ProductListViewModelMockTest`).
- Build domain objects with the **builders** in `core/builders/` (`ProductBuilder`, `PromotionBuilder`, `CartItemBuilder`). Don't construct entities ad hoc in tests.
- Coroutine tests use the shared `MainDispatcherRule` from `src/sharedTest/java/.../core/MainDispatcherRule.kt`.
- Flow assertions use **Turbine**.
- Time-sensitive code uses the `Clock` abstraction — inject `FakeSystemClock` in tests.

**Instrumentation tests (`src/androidTest/`)**
- Hilt-injected tests use `@HiltAndroidTest` + the custom `HiltTestRunner`.
- Network mocking goes through the helpers in `core/mockwebserver/`: `MockWebServerRule`, `MiniMarketApiDispatcher` (serves JSON from `androidTest/assets`), `ProductErrorDispatcher` (failure paths), `MockWebServerUrlHolder`.
- Compose UI tests use `createAndroidComposeRule` and look up nodes by **tags from `UiTestTag`** ([core/presentation/testing/UiTestTag.kt](app/src/main/java/com/aristidevs/cursotestingandroid/core/presentation/testing/UiTestTag.kt)). When adding a new UI test, add the tag constant to `UiTestTag` and apply it via `Modifier.testTag(...)` in the composable rather than hardcoding strings.
- Reference patterns:
  - DAO tests: `PromotionDaoTest`, `CartItemDaoTest`
  - Repository tests with MockWebServer: `ProductRepositoryImplTest`, `CartItemRepositoryImplTest`
  - Offline-first integration: `OfflineFirstIntegrationTest`
  - Compose screen tests: `SettingsScreenTest`
