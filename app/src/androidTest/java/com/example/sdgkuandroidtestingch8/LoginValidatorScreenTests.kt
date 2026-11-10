package com.example.sdgkuandroidtestingch8

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import org.junit.Rule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Before
import org.junit.Test


// Random commment for testing purposes
class LoginValidatorPage(composeTestRule: ComposeTestRule) {
    private val emailField = composeTestRule.onNodeWithTag("EmailField")
    private val passwordField = composeTestRule.onNodeWithTag("PasswordField")
    private val loginButton = composeTestRule.onNodeWithTag("LoginButton")
    private val loginFeedback = composeTestRule.onNodeWithTag("LoginFeedback")

    fun enterEmail(email: String) {
        emailField.performTextInput(email)
    }

    fun enterPassword(password: String) {
        passwordField.performTextInput(password)
    }

    fun clickLogin() {
        loginButton.performClick()
    }

    fun assertFeedback(expectedFeedback: String) {
        loginFeedback.assertTextEquals(expectedFeedback)
    }
}

class LoginValidatorScreenTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    val loginValidatorPage = LoginValidatorPage(composeTestRule)

    @Before
    fun setup() {
        // ARRANGE
        composeTestRule.setContent {
            LoginValidatorScreen()
        }
    }

    @Test
    fun emptyEmailShowsError() {
        // ACT
        loginValidatorPage.clickLogin()

        // ASSERT
        loginValidatorPage.assertFeedback("Email cannot be empty")
    }

    @Test
    fun invalidEmailShowsError() {
        // ACT
        loginValidatorPage.enterEmail("daniel.moreno")
        loginValidatorPage.clickLogin()

        // ASSERT
        loginValidatorPage.assertFeedback("Email is invalid")
    }

    @Test
    fun shortPasswordShowsError() {
        // ACT
        composeTestRule.onNodeWithTag("EmailField").performTextInput("daniel.moreno@sdgku.edu")
        composeTestRule.onNodeWithTag("PasswordField").performTextInput("invalid")

        composeTestRule.onNodeWithTag("LoginButton").performClick()

        // ASSERT
        composeTestRule.onNodeWithTag("LoginFeedback").assertTextEquals("Password must be at least 12 characters long")
    }

    @Test
    fun loginSuccessful() {
        // ACT
        composeTestRule.onNodeWithTag("EmailField").performTextInput("daniel.moreno@sdgku.edu")
        composeTestRule.onNodeWithTag("PasswordField").performTextInput("validpassword123")

        composeTestRule.onNodeWithTag("LoginButton").performClick()

        // ASSERT
        composeTestRule.onNodeWithTag("LoginFeedback").assertTextEquals("Login successful")
    }

    // Espresso XML Test
    @Test
    fun rememberMeCheckboxInitialState() {
        onView(withId(R.id.remember_me_checkbox))
            .check(matches(isDisplayed()))
            .check(matches(isNotChecked()))

        onView(withId(R.id.remember_me_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Remember me is unchecked")))
    }

    @Test
    fun rememberMeCheckboxChangesStateWhenChecked() {
        onView(withId(R.id.remember_me_checkbox))
            .check(matches(isDisplayed()))
            .check(matches(isNotChecked()))

        onView(withId(R.id.remember_me_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Remember me is unchecked")))

        onView(withId(R.id.remember_me_checkbox))
            .perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.remember_me_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("Remember me is checked")))
    }
}