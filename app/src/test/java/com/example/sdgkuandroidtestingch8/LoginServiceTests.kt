package com.example.sdgkuandroidtestingch8

import org.junit.Before
import org.junit.Test

// Real - UserStore uses database or network to get the data. It is the real implementation of the UserRepository interface.

// Fake - mimics the functionality of the UserStore for testing purposes. It implements the UserRepository interface and provides a hardcoded mapping of email addresses to passwords.

// Stub - Provides canned answers to calls made during the test, usually not responding at all to anything outside what's programmed in for the test

// Mock - Checks the collaboration between the SUT and its dependencies. Checks that methods were called

// Dummy - Is a logicless object to fill the parameters needed


class LoginServiceTests {
    private lateinit var service: LoginService

    @Before
    fun setup() {
        val fakeUserStore = FakeUserStore()
        service = LoginService(userStore = fakeUserStore)
    }

    @Test
    fun knownUserWithCorrectPassword_returnsSuccess() {
        val result = service.login("paola@sdgku.edu", "validpassword123")
        assert(result is LoginResult.Success)
    }

    @Test
    fun knownUserWithIncorrectPassword_returnsWrongPassword() {
        val result = service.login("paola@sdgku.edu", "wrongpassword")
        assert(result is LoginResult.WrongCredentials)
    }

    @Test
    fun emptyEmail_returnsInvalidEmailError() {
        val result = service.login("", "validpassword123")
        assert(result is LoginResult.Invalid)
        assert((result as LoginResult.Invalid).error == LoginError.EMPTY_EMAIL)
    }

    @Test
    fun invalidEmail_returnsInvalidEmailError() {
        val result = service.login("invalidemail", "validpassword123")
        assert(result is LoginResult.Invalid)
        assert((result as LoginResult.Invalid).error == LoginError.INVALID_EMAIL)
    }


}


// Fake of the UserStore for testing purposes
class FakeUserStore : UserRepository {
    private val users = mapOf(
        "paola@sdgku.edu" to "validpassword123"
    )

    override fun passwordFor(email: String): String? {
        return users[email]
    }
}
