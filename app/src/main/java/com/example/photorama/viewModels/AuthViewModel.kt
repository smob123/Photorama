package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.AuthRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context) : ViewModel() {
    private val userInfo = MutableLiveData<UserType>()

    private val authErrorMessage = MutableLiveData<ErrorMessage>()

    private val firebaseTokenUploaded = MutableLiveData<Boolean>()

    private val firebaseTokenUploadError = MutableLiveData<ErrorMessage>()

    private val loggedOut = MutableLiveData<Boolean>()

    private val logoutErrorMessage = MutableLiveData<ErrorMessage>()

    private val authRepo: AuthRepo

    init {
        authRepo = AuthRepo.getInstance(context)
    }

    fun getUserInfo() = userInfo as LiveData<UserType>

    fun getAuthErrorMessage() = authErrorMessage as LiveData<ErrorMessage>

    fun isFirebaseTokenUploaded() = firebaseTokenUploaded as LiveData<Boolean>

    fun getFirebaseTokenUploadError() = firebaseTokenUploadError as LiveData<ErrorMessage>

    fun isLoggedOut() = loggedOut as LiveData<Boolean>

    fun getLogoutErrorMessage() = logoutErrorMessage as LiveData<ErrorMessage>

    fun login(username: String, password: String, firebaseToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = authRepo.login(username, password, firebaseToken)

            if (user == null) {
                authErrorMessage.postValue(authRepo.authErrorMessage)
                return@launch
            }

            userInfo.postValue(user)
        }
    }

    fun signup(
        username: String,
        email: String,
        screenName: String,
        password: String,
        firebaseToken: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = authRepo.signup(
                username,
                email,
                screenName,
                password,
                firebaseToken
            )

            if (user == null) {
                authErrorMessage.postValue(authRepo.authErrorMessage)
                return@launch
            }

            userInfo.postValue(user)
        }
    }

    fun updateFirebaseToken(newToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val uploadToken = authRepo.updateFirebaseToken(newToken)

            if (!uploadToken) {
                firebaseTokenUploadError.postValue(authRepo.updateTokenError)
                return@launch
            }

            firebaseTokenUploaded.postValue(uploadToken)
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            val logout = authRepo.logout()

            if (!logout) {
                logoutErrorMessage.postValue(authRepo.logoutErrorMessage)
                return@launch
            }

            loggedOut.postValue(logout)
        }
    }
}
