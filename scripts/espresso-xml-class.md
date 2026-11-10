# Class Script: Testing an XML View with Espresso

**Time:** 20-25 minutes  
**Demo:** Add a "Remember me" checkbox to the existing login screen, then test it with Espresso.

This document is a live-coding guide. The snippets below are not applied to the app.
The checkbox is a UI demonstration only: it does not save credentials or change authentication.

## 1. Introduce the Two Testing APIs (3 minutes)

**Say:** "Our login screen uses Jetpack Compose. We already test it using `onNodeWithTag`. Today we will embed a traditional Android View defined in XML and test that part with Espresso."

Open `app/src/androidTest/java/com/example/sdgkuandroidtestingch8/LoginValidatorScreenTests.kt` and point to an existing test.

| UI being tested | How we find it                              | How we click it    |
| --------------- | ------------------------------------------- | ------------------ |
| Compose element | `onNodeWithTag("LoginButton")`              | `performClick()`   |
| Android View    | `onView(withId(R.id.remember_me_checkbox))` | `perform(click())` |

**Ask:** "Does a Compose test tag automatically become an Android resource ID?"

**Answer:** No. These APIs target different UI representations. Espresso targets Android Views; Compose tests target the semantics tree.

### Before class

- Sync the project and start an Android emulator or connect a device.
- Verify the existing login tests run.
- Open `app/build.gradle.kts`. Espresso, Compose UI testing, and the instrumentation runner are already configured; no new dependencies are needed.
- Keep the tests under `app/src/androidTest`, not `app/src/test`, because they need an Android runtime.

## 2. Create the XML View (4 minutes)

**Say:** "We only need two widgets: a checkbox we can click and a label we can assert against. Each receives an ID so Espresso can locate it."

Add these entries inside the existing `<resources>` element in `app/src/main/res/values/strings.xml`:

```xml
<string name="remember_me">Remember me</string>
<string name="remember_me_on">Remember me is on</string>
<string name="remember_me_off">Remember me is off</string>
```

Create the `layout` resource directory if needed, then create `app/src/main/res/layout/view_remember_me.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_horizontal"
    android:orientation="vertical">

    <CheckBox
        android:id="@+id/remember_me_checkbox"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:minHeight="48dp"
        android:text="@string/remember_me" />

    <TextView
        android:id="@+id/remember_me_status"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:accessibilityLiveRegion="polite"
        android:text="@string/remember_me_off" />
</LinearLayout>
```

**Point out:** `@+id` creates an ID. Kotlin and Espresso reference it through `R.id`. XML defines the widgets, but we still need to connect the interaction in Kotlin.

## 3. Embed It in the Login (5 minutes)

Open `app/src/main/java/com/example/sdgkuandroidtestingch8/MainActivity.kt`.

Add these imports, keeping the existing imports:

```kotlin
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.TextView
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.viewinterop.AndroidView
```

Add this composable at file level, before the preview function:

```kotlin
@Composable
private fun RememberMeView() {
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    AndroidView(
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.view_remember_me, null).apply {
                findViewById<CheckBox>(R.id.remember_me_checkbox)
                    .setOnCheckedChangeListener { _, isChecked -> rememberMe = isChecked }
            }
        },
        update = { view ->
            view.findViewById<CheckBox>(R.id.remember_me_checkbox).isChecked = rememberMe
            view.findViewById<TextView>(R.id.remember_me_status).setText(
                if (rememberMe) R.string.remember_me_on else R.string.remember_me_off
            )
        }
    )
}
```

Inside the existing `LoginValidatorScreen` column, insert this call between the password field and the login button:

```kotlin
RememberMeView()
```

**Say:** "`AndroidView` is the bridge. Its factory inflates the XML into actual Android Views. The checkbox listener changes Compose state, and `update` reflects that state in the checkbox and label."

**Clarify:** `rememberSaveable` supports restoring UI state during activity recreation. It is not a persistent account preference or credential store.

Run the app and click the checkbox manually. Show the label changing from "Remember me is off" to "Remember me is on" and back. Leave the existing login validation unchanged.

## 4. Write the Espresso Test (5 minutes)

Return to `LoginValidatorScreenTests.kt`. Keep the existing Compose rule, setup method, and tests. The setup already renders `LoginValidatorScreen`, including our embedded XML view.

Add these imports:

```kotlin
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
```

Add this method inside the existing test class:

```kotlin
@Test
fun rememberMeXmlViewTogglesStatus() {
    onView(withId(R.id.remember_me_checkbox))
        .check(matches(isDisplayed()))
        .check(matches(isNotChecked()))
    onView(withId(R.id.remember_me_status))
        .check(matches(withText(R.string.remember_me_off)))

    onView(withId(R.id.remember_me_checkbox)).perform(click())
    composeTestRule.waitForIdle()

    onView(withId(R.id.remember_me_checkbox)).check(matches(isChecked()))
    onView(withId(R.id.remember_me_status))
        .check(matches(withText(R.string.remember_me_on)))

    onView(withId(R.id.remember_me_checkbox)).perform(click())
    composeTestRule.waitForIdle()

    onView(withId(R.id.remember_me_checkbox)).check(matches(isNotChecked()))
    onView(withId(R.id.remember_me_status))
        .check(matches(withText(R.string.remember_me_off)))
}
```

**Walk through the pattern:**

1. **Arrange:** The existing `@Before` renders the screen. Verify the initial unchecked state.
2. **Act:** `onView(withId(...)).perform(click())` locates and clicks the XML checkbox.
3. **Assert:** `check(matches(...))` verifies the checkbox state and status text.
4. Repeat the interaction to verify toggling off works too.

**Say:** "Espresso synchronizes with the Android UI. Here the listener also triggers Compose state changes, so we explicitly wait for Compose to finish updating before checking the label. We do not need a fixed sleep."

## 5. Run It, Then Make It Fail (4 minutes)

Run the new method using the test gutter icon in Android Studio, or use this command from the project root with a device connected:

```sh
./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.example.sdgkuandroidtestingch8.LoginValidatorScreenTests#rememberMeXmlViewTogglesStatus'
```

**Expected:** One passing test. The checkbox starts off, switches on, and switches off again.

**Ask before running:** "After the first click, what should the label say?"

For a deliberate failure, change only the assertion immediately after the first click from `R.string.remember_me_on` to `R.string.remember_me_off`. Run it again.

**Explain:** "The view was found and clicked. The failure is an assertion mismatch: the expected text is wrong. Read the expected and actual values in the test report."

Restore `R.string.remember_me_on` and rerun. Then run the entire `LoginValidatorScreenTests` class to check that the original login tests still pass:

```sh
./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.example.sdgkuandroidtestingch8.LoginValidatorScreenTests'
```

### Quick troubleshooting

| Symptom                                                     | Check                                                                                                                                                                |
| ----------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `R.layout` or an ID is unresolved                           | Confirm the XML file location, fix resource errors, and rebuild. Use the app's `R`, not `android.R`.                                                                 |
| No connected devices                                        | Start an emulator or authorize the connected device before running instrumentation tests.                                                                            |
| Espresso cannot find the view                               | Confirm `RememberMeView()` is called inside the login column and the resource ID matches.                                                                            |
| The click fails because the view is not displayed           | Dismiss the keyboard and check the emulator viewport. This demo assumes the checkbox is visible; the outer scroll container is Compose, not an Android `ScrollView`. |
| The label assertion runs before the Compose update finishes | Keep `composeTestRule.waitForIdle()` after each click.                                                                                                               |

## 6. Student Exercise and Wrap-Up (2-4 minutes)

**Exercise:** Add a separate test called `rememberMeStartsUnchecked` that checks the initial checkbox state and label without clicking. Keep the shared setup so each test starts independently.

**Ask:**

- Why do we use `withId` here instead of `onNodeWithTag`?
- Which part performs an action, and which part makes an assertion?
- Does this passing test prove that the application remembers a user after sign-in?

**Close:** "We tested an XML-based Android View inside a Compose screen. Espresso uses matchers to find Views, actions to interact with them, and assertions to verify results. This test proves the checkbox UI works, not that authentication or persistent storage works."