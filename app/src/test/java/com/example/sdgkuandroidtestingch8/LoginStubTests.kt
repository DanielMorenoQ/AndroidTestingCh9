package com.example.sdgkuandroidtestingch8

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

// Stub - Stubs feed canned answers to calls made during the test, usually not responding at all to anything outside what's programmed in for the test.
class LoginStubTests {
    /// Tests using Mockito
    @Test
    fun login_withWrongPassword_returnsWrongCredentials() {
        val repo = mock<UserRepository>()
        whenever(repo.passwordFor("daniel@sd.edu")).thenReturn("validpassword123")
        val service = LoginService(userStore = repo)
        val result = service.login("daniel@sd.edu", "wrongpassword")
        assertEquals(LoginResult.WrongCredentials, result)
    }

    @Test
    fun login_withWrongUser_returnsWrongCredentials() {
        val repo = mock<UserRepository>()
        whenever(repo.passwordFor("wronguser@sd.edu")).thenReturn(null)
        val service = LoginService(userStore = repo)
        val result = service.login("wronguser@sd.edu", "wrongpassword")
        assertEquals(LoginResult.WrongCredentials, result)
    }
}
