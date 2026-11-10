package com.example.sdgkuandroidtestingch8

sealed interface LoginResult {
    object Success : LoginResult
    object WrongCredentials : LoginResult
    data class Invalid(val error: LoginError) : LoginResult
}

// Dependency: Use UserStore
class LoginService(private val userStore: UserRepository = UserStore() ){

    fun login(email: String, password: String): LoginResult {
        LoginValidator().validate(email, password)?.let { error ->
            return LoginResult.Invalid(error)
        }
        val storedPassword = userStore.passwordFor(email)
        return if (storedPassword != null && storedPassword == password) {
            LoginResult.Success
        } else {
            LoginResult.WrongCredentials
        }
    }
}

