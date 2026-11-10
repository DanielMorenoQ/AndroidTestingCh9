package com.example.sdgkuandroidtestingch8

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.CheckBox
import android.widget.TextView
import com.example.sdgkuandroidtestingch8.ui.theme.SDGKUAndroidTestingCh8Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SDGKUAndroidTestingCh8Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginValidatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginValidatorScreen(
    modifier: Modifier = Modifier,
    loginValidator: LoginValidator = LoginValidator()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        OutlinedTextField(
            modifier = Modifier.testTag("EmailField"),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        OutlinedTextField(
            modifier = Modifier.testTag("PasswordField"),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        RememberMeView()

        Button(
            onClick = {
            feedback = when (loginValidator.validate(email, password)) {
                LoginError.EMPTY_EMAIL -> "Email cannot be empty"
                LoginError.INVALID_EMAIL -> "Email is invalid"
                LoginError.SHORT_PASSWORD -> "Password must be at least 12 characters long"
                null -> "Login successful"
            }
        },
            Modifier.testTag("LoginButton")
        ) {
            Text("Login")
        }

        if (feedback.isNotEmpty()) {
            Text(
                modifier = Modifier.testTag("LoginFeedback"),
                text = feedback
            )
        }
    }
}

@Composable
private fun RememberMeView() {
    var rememberMe by remember { mutableStateOf(false) }

    AndroidView (
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.view_remember_me, null, false).apply {
                findViewById<CheckBox>(R.id.remember_me_checkbox).setOnCheckedChangeListener { _, isChecked ->
                    rememberMe = isChecked
                }
            }
        },
        update = { view ->
            view.findViewById<CheckBox>(R.id.remember_me_checkbox).isChecked = rememberMe
            val text = if (rememberMe) "Remember me is checked" else "Remember me is unchecked"
            view.findViewById<TextView>(R.id.remember_me_text).text = text
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SDGKUAndroidTestingCh8Theme {
        LoginValidatorScreen()
    }
}