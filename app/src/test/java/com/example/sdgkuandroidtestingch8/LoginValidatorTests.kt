package com.example.sdgkuandroidtestingch8

import org.junit.Test

class LoginValidatorTests {
    @Test
    fun emptyEmail_isRejected() {
        val validator = LoginValidator()
        val result = validator.validate("", "validPassword123")

        assert(result == LoginError.EMPTY_EMAIL)
    }

    @Test
    fun invalidEmail_isRejected() {
        val validator = LoginValidator()
        val result = validator.validate("daniel.moreno", "validPassword123")

        assert(result == LoginError.INVALID_EMAIL)
    }

    @Test
    fun shortPassword_isRejected() {
        val validator = LoginValidator()
        val result = validator.validate("daniel.moreno@sdgku.com", "invalid")

        assert(result == LoginError.SHORT_PASSWORD)
    }

    @Test
    fun validEmailAndPassword_isAccepted() {
        val validator = LoginValidator()
        val result = validator.validate("daniel.moreno@sdgku.com", "validPassword123")

        // No errors
        assert(result == null)
    }
}
