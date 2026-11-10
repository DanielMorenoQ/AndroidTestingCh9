# Course Script — Stubs, Mocks & Dependency Injection (Class 3, 2.5 hours)

**Project:** `AndroidTestingSDGKU`
**Package:** `com.example.androidtestingsdgku`
**Builds on:** `LoginValidator`, `ShoppingCart`, `ShopScreen` from classes 1–2.
Your project currently validates inline with `LoginValidator` and has no auth collaborator, so
the demo first builds a small `LoginService` + `UserStore` to inject and mock.

> Roadmap item #3: *Mocks, stubs and fakes* — plus the **Dependency Injection** principle that
> makes them easy to use.

---

## Timeline

| Time      | Block      | Activity                                                          |
| --------- | ---------- | ----------------------------------------------------------------- |
| 0:00–0:10 | Welcome    | Recap classes 1–2, check the project builds                       |
| 0:10–0:20 | UI warm-up | Run an existing login UI scenario locally with Robolectric        |
| 0:20–0:40 | Theory     | Slides 1–21: test doubles + DI                                    |
| 0:40–0:55 | Live demo  | Refactor `LoginService` behind an interface + a hand-written fake |
| 0:55–1:05 | Break      |                                                                   |
| 1:05–1:40 | Exercise 1 | DI + fakes + Mockito stubs/mocks on the auth layer                |
| 1:40–2:15 | Exercise 2 | `ShopViewModel` + `ProductRepository` tested with doubles         |
| 2:15–2:30 | Wrap-up    | Compare doubles, pitfalls, optional bonus, homework               |

The 5-minute setup is included in Welcome. The comparison discussion is included in
Exercise 1. Wrap-up includes 5 minutes of key takeaways and 10 minutes for homework,
questions, and the optional bonus if time allows. Total: **150 minutes**.

---

## Opening demo — an existing UI test with Robolectric (10 min)

**Goal:** Reuse the `emptyEmailShowsError` scenario from `LoginValidatorScreenTests`,
but run it on the local JVM without starting an emulator. Keep the existing device test.

**Say:** "We already test this screen on Android. Robolectric supplies an Android-like
environment on the JVM so we can run this same UI scenario locally. Compose's testing API
still finds and clicks the elements; Robolectric provides the runtime. It is not Mockito."

### 1. Prepare the local UI test dependencies (3 min)

In `app/build.gradle.kts`, add this inside the existing `android` block:

```kotlin
testOptions {
    unitTests.isIncludeAndroidResources = true
}
```

Add these entries to the existing `dependencies` block:

```kotlin
testImplementation("org.robolectric:robolectric:4.16")
testImplementation(platform(libs.androidx.compose.bom))
testImplementation(libs.androidx.compose.ui.test.junit4)
```

Keep the existing `testImplementation(libs.junit)` and
`debugImplementation(libs.androidx.compose.ui.test.manifest)` entries. The latter supplies
the test host activity for `createComposeRule()`. Dependencies in `androidTestImplementation`
are not automatically available to local tests in `src/test`.

**Instructor prep:** Sync and run the example before class so dependency and Robolectric
SDK downloads do not consume the demo. Use the project's supported Gradle JDK (at least
JDK 17 for this Robolectric/API 35 example). API 35 is explicitly selected below so the test
does not default to this project's newer target SDK, which the pinned Robolectric version
may not support. This tests API 35 behaviour, not every supported Android version.

### 2. Adapt the existing test (4 min)

Create `app/src/test/java/com/example/sdgkuandroidtestingch8/LoginValidatorRobolectricTest.kt`:

```kotlin
package com.example.sdgkuandroidtestingch8

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@LooperMode(LooperMode.Mode.PAUSED)
class LoginValidatorRobolectricTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyEmailShowsError() {
        composeTestRule.setContent {
            LoginValidatorScreen()
        }

        composeTestRule.onNodeWithTag("LoginButton").performClick()

        composeTestRule.onNodeWithTag("LoginFeedback")
            .assertTextEquals("Email cannot be empty")
    }
}
```

This opening example uses the **current workspace package**. Later course examples retain
the original `com.example.androidtestingsdgku` package; adapt those paths and packages when
using them in this workspace.

**Point out:** The existing test uses `LoginValidatorPage`, which lives in `androidTest`.
Local tests cannot import that source set, so this short example calls the same underlying
Compose actions and assertions directly. There is no need to move the helper for this demo.
`PAUSED` looper mode lets the test infrastructure control queued main-thread work.

### 3. Run and compare (3 min)

Run the new class from the IDE gutter, or from the project root:

```bash
./gradlew :app:testDebugUnitTest --tests '*LoginValidatorRobolectricTest'
```

**Expected:** One passing local UI test; no connected device required. Open
`app/build/reports/tests/testDebugUnitTest/index.html` to inspect the result.

**Ask:** "What stayed the same, and what changed?"

- Same screen, click, and expected error message.
- Different source set (`test` instead of `androidTest`), runner, and runtime.
- Robolectric does not replace device coverage for real rendering, hardware, or platform
  integration. Keep the instrumentation tests as well.

**Transition:** "Robolectric provides the Android runtime for a local test. Next, Mockito
will let us replace specific collaborators, such as a repository. Dependency injection
makes those collaborators easy to swap."

---

## Theory in one page (the slides go deeper)

**A test double** is a stand-in for a real collaborator of the class under test (the *SUT*,
system under test). We use them when the real thing is slow, non-deterministic, has side
effects (network, DB, time), or is hard to push into an error state.

**The five doubles (Meszaros / Fowler):**

| Double    | What it is                                        | Verifies                                 |
| --------- | ------------------------------------------------- | ---------------------------------------- |
| **Dummy** | Passed to fill a parameter, never used            | nothing                                  |
| **Stub**  | Returns canned answers to calls                   | **state** (assert on SUT result)         |
| **Fake**  | A real but lightweight implementation (in-memory) | state                                    |
| **Spy**   | A stub that also records how it was called        | state + calls                            |
| **Mock**  | Pre-programmed with expectations                  | **behaviour** (assert the SUT called it) |

**Stub vs Mock** is the key distinction:
- A **stub** feeds data *in* → you assert the SUT's **state/return** (*state verification*).
- A **mock** records calls *out* → you assert the SUT **interacted** with it correctly
  (*interaction verification*).

**Dependency Injection (DI)** is how we swap the real collaborator for a double: a class
receives its dependencies from outside (usually the constructor) instead of creating them.
That follows the **Dependency Inversion Principle** — depend on an abstraction (interface),
not a concrete class.

---

## Setup — add the mocking library (5 min)

Add to `gradle/libs.versions.toml` under `[libraries]`:

```toml
mockito-kotlin = { group = "org.mockito.kotlin", name = "mockito-kotlin", version = "5.4.0" }
mockito-core = { group = "org.mockito", name = "mockito-core", version = "5.12.0" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version = "1.9.0" }
```

Add to `app/build.gradle.kts`:

```kotlin
testImplementation(libs.mockito.kotlin)              // Kotlin-friendly Mockito API
testImplementation(libs.mockito.core)                // JVM mock maker, including final classes
testImplementation(libs.kotlinx.coroutines.test)     // for the suspend bonus
// For device tests, use these instead of mockito-core in androidTest:
// androidTestImplementation("org.mockito:mockito-android:5.12.0")
// androidTestImplementation(libs.mockito.kotlin) {
//     exclude(group = "org.mockito", module = "mockito-core")
// }
```

Sync Gradle.

Mockito-Kotlin is a Kotlin-friendly wrapper around Mockito, not a separate mocking engine.
Use the `org.mockito.kotlin` imports below, especially `any()` for non-null Kotlin parameters.
These pinned versions require JDK 11 or newer. Mockito 5's default JVM mock maker supports
final Kotlin classes, including the fake used in the spy example; Android's mock maker has
different limitations, so keep these examples in JVM unit tests.

---

# Live demo — build a collaborator, then inject it (15 min)

Right now `MainActivity` calls `LoginValidator.validate(...)` directly — pure logic with no
collaborator, so there's nothing to inject or mock yet. First we add a small auth layer worth
isolating: a `LoginService` that checks credentials against a `UserStore`. Then we apply DI so
tests can swap the store for a double.

### 0. Build-first — the collaborator we'll inject

`LoginValidator` already exists from class 1. Add a service and a data source around it
(three new files in `app/src/main/java/com/example/androidtestingsdgku/`):

```kotlin
// LoginResult.kt
package com.example.androidtestingsdgku

sealed interface LoginResult {
    data object Success : LoginResult
    data object WrongCredentials : LoginResult
    data class Invalid(val error: LoginError) : LoginResult
}
```

```kotlin
// UserStore.kt — a concrete data source (hardcoded for now)
package com.example.androidtestingsdgku

class UserStore {
    private val users = mapOf("ana@example.com" to "password1234")
    fun passwordFor(email: String): String? = users[email]
}
```

```kotlin
// LoginService.kt — orchestrates validation + the credential lookup
package com.example.androidtestingsdgku

class LoginService(private val store: UserStore = UserStore()) {
    fun login(email: String, password: String): LoginResult {
        LoginValidator().validate(email.trim(), password)?.let { return LoginResult.Invalid(it) }
        return if (store.passwordFor(email.trim()) == password) LoginResult.Success
        else LoginResult.WrongCredentials
    }
}
```

> Now there's a real collaboration (`LoginService` → `UserStore`) worth testing in isolation.
> It depends on the **concrete** `UserStore` — the next steps turn that into an abstraction.

### 0.1. Create the baseline tests before refactoring

There is no `LoginServiceTest` yet. Create it now in
`app/src/test/java/com/example/androidtestingsdgku/LoginServiceTest.kt`, using the same
package as your service (in this workspace, `com.example.sdgkuandroidtestingch8`).

```kotlin
package com.example.androidtestingsdgku

import org.junit.Assert.assertEquals
import org.junit.Test

class LoginServiceTest {
    private val service = LoginService()

    @Test
    fun knownUserWithCorrectPassword_returnsSuccess() {
        assertEquals(
            LoginResult.Success,
            service.login("ana@example.com", "password1234")
        )
    }

    @Test
    fun knownUserWithWrongPassword_returnsWrongCredentials() {
        assertEquals(
            LoginResult.WrongCredentials,
            service.login("ana@example.com", "wrongpassword123")
        )
    }

    @Test
    fun invalidEmail_returnsValidationError() {
        assertEquals(
            LoginResult.Invalid(LoginError.INVALID_EMAIL),
            service.login("not-an-email", "password1234")
        )
    }
}
```

Run these three tests **before** extracting the interface:

```bash
./gradlew :app:testDebugUnitTest --tests '*LoginServiceTest'
```

**Expected:** Three passing tests. Stop and fix any failures before refactoring.
These baseline tests exercise the service with the concrete, in-memory `UserStore`;
they do not use doubles yet. Keep them unchanged during steps 1-3.

**Point out:** The current validator requires at least 12 password characters, uses an
instance method, and returns the top-level `LoginError` enum. The service trims the email
before validation and lookup so the later whitespace interaction test reaches the repository.
The wrong-password test uses a sufficiently long password to test credential rejection,
not password-length validation. The hardcoded credentials are teaching data only.

This baseline step is part of the existing 15-minute live demo; prepare the snippets before
class and run them live. The 150-minute schedule is unchanged.

### Say this

> "`LoginService` is welded to `UserStore`. To test it in isolation I want to hand it a fake.
> Step one of DI: depend on an interface, not a class."

### 1. Extract the interface

`app/src/main/java/com/example/androidtestingsdgku/UserRepository.kt`

```kotlin
package com.example.androidtestingsdgku

interface UserRepository {
    fun passwordFor(email: String): String?
}
```

### 2. Make the real store implement it

`UserStore.kt`

```kotlin
class UserStore : UserRepository {
    private val users = mapOf("ana@example.com" to "password1234")
    override fun passwordFor(email: String): String? = users[email]
}
```

### 3. Inject the abstraction

`LoginService.kt`

```kotlin
class LoginService(private val users: UserRepository = UserStore()) {
    fun login(email: String, password: String): LoginResult {
        LoginValidator().validate(email.trim(), password)?.let { return LoginResult.Invalid(it) }
        return if (users.passwordFor(email.trim()) == password) LoginResult.Success
        else LoginResult.WrongCredentials
    }
}
```

Rerun the three `LoginServiceTest` tests created in step 0.1 using the same command.
They should still pass without changing their assertions. **Refactor under a test net:**
we established the baseline before changing the dependency type, then checked it afterward.

### 4. A hand-written fake (the simplest double)

```kotlin
class FakeUserRepository(
    private val users: MutableMap<String, String> = mutableMapOf()
) : UserRepository {
    fun withUser(email: String, password: String) = apply { users[email] = password }
    override fun passwordFor(email: String): String? = users[email]
}
```

```kotlin
@Test
fun login_withFakeRepository_succeeds() {
    val repo = FakeUserRepository().withUser("ana@example.com", "password1234")
    val service = LoginService(repo)
    assertEquals(LoginResult.Success, service.login("ana@example.com", "password1234"))
}
```

> **Point out:** no Mockito yet. A fake is often the clearest double — it's just a class.

---

# Exercise 1 — Fakes, stubs and mocks on the auth layer (35 min)

All tests go in `app/src/test/java/com/example/androidtestingsdgku/`.

### Task 1.1 — A stub with Mockito (state verification)

A **stub** feeds canned data; you assert on the **result**.

```kotlin
package com.example.androidtestingsdgku

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginServiceStubTest {

    @Test
    fun login_wrongPassword_returnsWrongCredentials() {
        val repo = mock<UserRepository>()
        whenever(repo.passwordFor("ana@example.com")).thenReturn("password1234")

        val service = LoginService(repo)

        assertEquals(
            LoginResult.WrongCredentials,
            service.login("ana@example.com", "not-the-password")
        )
    }

    @Test
    fun login_unknownUser_returnsWrongCredentials() {
        val repo = mock<UserRepository>()
        whenever(repo.passwordFor(any())).thenReturn(null)

        val service = LoginService(repo)

        assertEquals(LoginResult.WrongCredentials, service.login("ghost@x.com", "whatever1234"))
    }
}
```

### Task 1.2 — A mock (interaction verification)

A **mock** proves the SUT *talked to* its collaborator the right way. Verify that a validation
failure **never hits the repository** (short-circuit), and a valid attempt looks it up exactly once.

```kotlin
package com.example.androidtestingsdgku

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.junit.Test

class LoginServiceMockTest {

    @Test
    fun invalidEmail_neverTouchesRepository() {
        val repo = mock<UserRepository>()

        LoginService(repo).login("not-an-email", "password1234")

        verify(repo, never()).passwordFor(any())
        verifyNoMoreInteractions(repo)
    }

    @Test
    fun validAttempt_looksUpOnce_withTrimmedEmail() {
        val repo = mock<UserRepository>()
        whenever(repo.passwordFor("ana@example.com")).thenReturn("password1234")

        LoginService(repo).login("  ana@example.com ", "password1234")

        verify(repo, times(1)).passwordFor("ana@example.com")
        verifyNoMoreInteractions(repo)
    }
}
```

> **Teaching point:** the second test is an *interaction* test — it asserts the email was
> trimmed **before** the lookup, something a state test can't see directly.

### Task 1.3 — Compare the three doubles (discussion, 5 min)

- The **fake** reads naturally and is reusable across tests.
- The **stub** is quickest for a one-off canned answer.
- The **mock** is the only one that proves *how* the collaborator was used.
- Rule of thumb: **prefer fakes/stubs; reach for a mock when the interaction itself is the behaviour.**

---

# Exercise 2 — A shop that depends on a repository (35 min)

Now apply DI to the Shop. The screen should not know *where* products come from.

### Task 2.1 — The abstraction + a real implementation

`app/src/main/java/com/example/androidtestingsdgku/shop/ProductRepository.kt`

```kotlin
package com.example.androidtestingsdgku.shop

interface ProductRepository {
    fun products(): List<Product>
}

class InMemoryProductRepository : ProductRepository {
    override fun products(): List<Product> = Catalog.items
}
```

### Task 2.2 — A thin, framework-free view model

Keep it a plain class so it unit-tests on the JVM with no Android.

`app/src/main/java/com/example/androidtestingsdgku/shop/ShopViewModel.kt`

```kotlin
package com.example.androidtestingsdgku.shop

class ShopViewModel(
    private val repository: ProductRepository,
    private val cart: ShoppingCart = ShoppingCart()
) {
    val products: List<Product> get() = repository.products()

    fun add(product: Product) = cart.add(product)
    fun subtotal(): Double = cart.subtotal()
}
```

### Task 2.3 — Test it with a fake

`app/src/test/java/com/example/androidtestingsdgku/shop/ShopViewModelFakeTest.kt`

```kotlin
package com.example.androidtestingsdgku.shop

import org.junit.Assert.assertEquals
import org.junit.Test

class FakeProductRepository(private val items: List<Product>) : ProductRepository {
    override fun products(): List<Product> = items
}

class ShopViewModelFakeTest {

    private val delta = 0.001

    @Test
    fun exposesProductsFromRepository() {
        val repo = FakeProductRepository(listOf(Product("p1", "Keyboard", 80.0)))
        val vm = ShopViewModel(repo)
        assertEquals(1, vm.products.size)
        assertEquals("Keyboard", vm.products.first().name)
    }

    @Test
    fun subtotalAppliesCartDiscount() {
        val repo = FakeProductRepository(
            listOf(Product("p3", "Monitor", 180.0), Product("p4", "Headset", 120.0))
        )
        val vm = ShopViewModel(repo)
        vm.products.forEach { vm.add(it) }     // 300 -> 10% -> 270
        assertEquals(270.0, vm.subtotal(), delta)
    }
}
```

### Task 2.4 — Test it with a Mockito mock

`app/src/test/java/com/example/androidtestingsdgku/shop/ShopViewModelMockTest.kt`

```kotlin
package com.example.androidtestingsdgku.shop

import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

class ShopViewModelMockTest {

    @Test
    fun readsProductsFromRepositoryOnce() {
        val repo = mock<ProductRepository>()
        whenever(repo.products()).thenReturn(listOf(Product("p2", "Mouse", 45.0)))

        val vm = ShopViewModel(repo)
        val first = vm.products
        assertEquals(1, first.size)

        verify(repo, times(1)).products()
    }
}
```

### Task 2.5 — Wire the real repository into the app (manual DI)

Keep production using the real implementation; only tests pass doubles. The `ShopActivity`
becomes the *composition root* for this screen:

```kotlin
class ShopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ShopViewModel(InMemoryProductRepository())   // wiring happens here
        setContent { MaterialTheme { ShopScreen(viewModel) } }
    }
}
```

Update `ShopScreen` to take the view model (products from `viewModel.products`, each Add calls
`viewModel.add(product)`, subtotal from `viewModel.subtotal()`). The Compose test from class 2
still drives it through the tags — the screen doesn't care that the data now comes via DI.

> **Say this:** "The Activity is the only place that knows the *concrete* repository. Everything
> below depends on the interface — that's the composition root pattern."

---

## Bonus — a suspending repository, mocked (if time allows)

Real repositories are asynchronous. Make the lookup a `suspend` function and test it with
`runTest` + a Mockito mock. Inside `runTest`, use ordinary `whenever` and `verify` for
suspend calls; no coroutine-specific Mockito verification API is needed.

```kotlin
interface AuthApi {
    suspend fun passwordFor(email: String): String?
}

class SuspendLoginService(private val api: AuthApi) {
    suspend fun login(email: String, password: String): LoginResult {
        LoginValidator().validate(email.trim(), password)?.let { return LoginResult.Invalid(it) }
        return if (api.passwordFor(email.trim()) == password) LoginResult.Success
        else LoginResult.WrongCredentials
    }
}
```

```kotlin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Test
fun suspendingLookup_isAwaited() = runTest {
    val api = mock<AuthApi>()
    whenever(api.passwordFor("ana@example.com")).thenReturn("password1234")

    val result = SuspendLoginService(api).login("ana@example.com", "password1234")
    assertEquals(LoginResult.Success, result)

    verify(api, times(1)).passwordFor("ana@example.com")
}
```

Also try a **spy** on the fake to combine real behaviour with verification:

```kotlin
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

val realRepo = FakeUserRepository().withUser("ana@example.com", "password1234")
val spied = spy(realRepo)
LoginService(spied).login("ana@example.com", "password1234")
verify(spied).passwordFor("ana@example.com")   // real data + recorded call
```

---

## Wrap-up — 15 min (5 min recap + 10 min bonus, questions and homework)

**Pitfalls to call out:**
1. **Over-mocking** — mocking everything makes tests mirror the implementation and break on
   every refactor. Prefer fakes/stubs; mock only at real boundaries (network, DB, time).
2. **Don't mock types you don't own** — wrap third-party SDKs behind your own interface, then
   fake/mock that.
3. **Don't mock value objects** (`Product`, `LoginResult`) — just construct them.
4. **A mock with no verification is just a stub** — if you only need data, say "stub".

**Key takeaways:**
1. A test double stands in for a real collaborator.
2. Stub = state verification; Mock = interaction verification.
3. DI (constructor injection on an interface) is what makes doubles possible.
4. The composition root (the Activity) is the only place that knows concrete types.

## Homework

1. Refactor `LoginService` to use a `suspend` `AuthApi` and test it with `runTest` + Mockito's `verify`.
2. Add a `FailingProductRepository` that throws, and write a test proving `ShopViewModel`
   surfaces the error the way you decide (empty list vs rethrow) — TDD the decision.
3. Replace one of your Mockito mocks with a hand-written fake and argue which reads better.

## Commands cheat sheet

```bash
# all JVM unit tests (fakes, stubs, mocks run here)
./gradlew :app:testDebugUnitTest

# one class
./gradlew :app:testDebugUnitTest --tests "*LoginServiceMockTest"

# report
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Mockito cheat sheet

```kotlin
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

val repo = mock<UserRepository>()
whenever(repo.passwordFor("a@b.com")).thenReturn("x")
whenever(repo.passwordFor(any())).thenThrow(IllegalStateException("Store unavailable"))
verify(repo).passwordFor("a@b.com")
verify(repo, never()).passwordFor(any())
verifyNoMoreInteractions(repo)
val defaultAnswers = mock<UserRepository>()
runTest {
    val api = mock<AuthApi>()
    whenever(api.passwordFor(any())).thenReturn("x")
    SuspendLoginService(api).login("ana@example.com", "password1234")
    verify(api).passwordFor("ana@example.com")
}
val spied = spy(FakeUserRepository())
doReturn("x").whenever(spied).passwordFor("a@b.com")
```

These are independent API examples, not one runnable test: verification requires a preceding
SUT call, and the positive and negative verifications illustrate different scenarios.
Mockito mocks return default answers for unstubbed calls (for example, `null` for this
nullable password result); there is no separate "relaxed" flag. Use `doReturn(...).whenever(spy)`
when stubbing a spy to avoid executing its real method during setup. The exception example
uses an unchecked exception because `passwordFor` does not declare a checked exception.
