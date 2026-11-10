package com.example.sdgkuandroidtestingch8

enum class LoginError {
    EMPTY_EMAIL,
    INVALID_EMAIL,
    SHORT_PASSWORD
}

class LoginValidator {
    // validating format of email using regex range from alphanumeric characters, special characters, and domain name
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun validate(email: String, password: String): LoginError? {
        if (!email.isNotEmpty()) {
            return LoginError.EMPTY_EMAIL
        }

        if (!email.matches(emailRegex)) {
            return LoginError.INVALID_EMAIL
        }

        if (password.length < 12) {
            return LoginError.SHORT_PASSWORD
        }

        return null
    }
}