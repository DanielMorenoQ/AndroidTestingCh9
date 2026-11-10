package com.example.sdgkuandroidtestingch8

import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

// Mock proves a SUT talked to a collaborator. It is a test double that records the interactions with it,
// allowing you to verify that the SUT made the expected calls to the mock object.
class LoginMockTests {
    @Test
    fun invalidEmail_neverCallsUserRepository() {
        val repo = mock<UserRepository>()
        val service = LoginService(userStore = repo)

        service.login("notanemail", "validpassword123")

        verify(repo, never()).passwordFor(any())
        verifyNoInteractions(repo)
    }

    @Test
    fun validAttempt_looksUpOnce_withValidEmail() {
        val repo = mock<UserRepository>()
        whenever(repo.passwordFor("daniel@sd.edu")).thenReturn("validpassword123")

        val service = LoginService(userStore = repo)
        service.login("daniel@sd.edu", "validpassword123")

        verify(repo, times(1)).passwordFor("daniel@sd.edu")
        verifyNoMoreInteractions(repo)
    }
}
