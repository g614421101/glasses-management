package com.glasses.app.data.model

data class RegisterRequest(
    val inviteCode: String,
    val username: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)
