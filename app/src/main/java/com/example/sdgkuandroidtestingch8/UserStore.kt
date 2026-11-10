package com.example.sdgkuandroidtestingch8



interface UserRepository {
    fun passwordFor(email: String): String?
}

// Real implementation of a user store that would normally be backed by a database or network call
class UserStore : UserRepository {
    private val users = mapOf(
        "daniel.moreno@sdgku.edu" to "validpassword123"
    )

    override fun passwordFor(email: String): String? {
        return users[email]
    }
}