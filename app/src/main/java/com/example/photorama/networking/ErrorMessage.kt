package com.example.photorama.networking

enum class MESSAGE {
    CONNECTION_ERROR,
    AUTH_ERROR,
    SIGNUP_EMAIL_ALREADY_USED,
    SIGNUP_USERNAME_ALREADY_USED,
    FIREBASE_TOKEN_ERROR,
    LOGOUT_ERROR,
    REMOVE_AVATAR_ERROR
}

class ErrorMessage(private val messageType: MESSAGE) {
    fun getMessage(): String {
        return when (messageType) {
            MESSAGE.CONNECTION_ERROR -> "Couldn't connect to the network"
            MESSAGE.AUTH_ERROR -> "Invalid credentials"
            MESSAGE.SIGNUP_EMAIL_ALREADY_USED -> "The provided email address is already attached to an account"
            MESSAGE.SIGNUP_USERNAME_ALREADY_USED -> "The provided username is already attached to an account"
            MESSAGE.FIREBASE_TOKEN_ERROR -> "Couldn't upload the new FireBase token"
            MESSAGE.LOGOUT_ERROR -> "An error occurred while logging out"
            MESSAGE.REMOVE_AVATAR_ERROR -> "Couldn't remove avatar"
        }
    }
}
