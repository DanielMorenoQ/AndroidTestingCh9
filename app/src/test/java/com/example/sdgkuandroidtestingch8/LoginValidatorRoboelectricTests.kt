package com.example.sdgkuandroidtestingch8

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

class LoginValidatorRobolectricTests {

    @RunWith(RobolectricTestRunner::class)
    @Config(sdk = [35])
    @LooperMode(LooperMode.Mode.PAUSED)
    class LoginValidatorRobolectricTests {
        @get:Rule
        val composeTestResult = createComposeRule()

        @Before
        fun setup() {
            composeTestResult.setContent {
                LoginValidatorScreen()
            }
        }

        @Test
        fun emptyEmailShowsError() {
            // ACT
            composeTestResult.onNodeWithTag("LoginButton").performClick()

            // ASSERT
            composeTestResult.onNodeWithTag("LoginFeedback").assertTextEquals("Email cannot be empty")
        }

        @Test
        fun shortPasswordShowsError() {
            // ACT
            composeTestResult.onNodeWithTag("EmailField").performTextInput("daniel.moreno@sdgku.edu")
            composeTestResult.onNodeWithTag("PasswordField").performTextInput("invalid")

            composeTestResult.onNodeWithTag("LoginButton").performClick()

            // ASSERT
            composeTestResult.onNodeWithTag("LoginFeedback").assertTextEquals("Password must be at least 12 characters long")
        }

        @Test
        fun loginSuccessful() {
            // ACT
            composeTestResult.onNodeWithTag("EmailField").performTextInput("daniel.moreno@sdgku.edu")
            composeTestResult.onNodeWithTag("PasswordField").performTextInput("validpassword123")

            composeTestResult.onNodeWithTag("LoginButton").performClick()

            // ASSERT
            composeTestResult.onNodeWithTag("LoginFeedback").assertTextEquals("Login successful")
        }
    }
}