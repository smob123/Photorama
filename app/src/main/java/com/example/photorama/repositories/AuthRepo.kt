package com.example.photorama.repositories

import android.content.Context
import com.example.photorama.LoginMutation
import com.example.photorama.SignupMutation
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.networking.MESSAGE
import com.example.photorama.networking.Mutations
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepo(private val context: Context) {

    /**
     * singleton pattern
     */
    companion object {
        private lateinit var instance: AuthRepo

        fun getInstance(context: Context): AuthRepo {
            if (!this::instance.isInitialized) {
                instance = AuthRepo(context)
            }

            return instance
        }
    }

    lateinit var authErrorMessage: ErrorMessage
    lateinit var updateTokenError: ErrorMessage
    lateinit var logoutErrorMessage: ErrorMessage

    suspend fun login(username: String, password: String, firebaseToken: String): UserType? =
        suspendCoroutine {
            Mutations(context).login(username, password, firebaseToken, onCompleted = { err, res ->
                if (err != null || res == null) {


                    // check if the error message has to do with the user's credentials
                    if (err!!.contains("Invalid credentials")) {
                        authErrorMessage = ErrorMessage(MESSAGE.AUTH_ERROR)
                    } else {
                        // otherwise it must be a connection error
                        authErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    }

                    it.resume(null)
                    return@login
                } else if (res.Login() == null) {
                    authErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(null)
                    return@login
                }

                val id = res.Login()!!.id()!!
                val uName = res.Login()!!.username()!!
                val screenName = res.Login()!!.screenName()!!
                val avatar = res.Login()!!.avatar()!!
                val posts = res.Login()!!.posts()!!
                val followers = res.Login()!!.followers() as List<String>
                val following = res.Login()!!.following() as List<String>

                val user = UserType(
                    id,
                    uName,
                    screenName,
                    avatar,
                    posts,
                    ArrayList(followers),
                    ArrayList(following)
                )

                cacheLoginInfo(res)
                it.resume(user)
            })
        }

    suspend fun signup(
        username: String,
        email: String,
        screenName: String,
        password: String,
        firebaseToken: String
    ): UserType? =
        suspendCoroutine {
            Mutations(context).signup(
                username, email, screenName, password, firebaseToken,
                onCompleted = { err, res ->
                    if (err != null || res == null) {
                        // possible error messages
                        val emailErr =
                            "The provided email address is already attached to an account"
                        val usernameErr =
                            "The provided username is already attached to an account"

                        if (err!!.contains(emailErr)) {
                            authErrorMessage = ErrorMessage(MESSAGE.SIGNUP_EMAIL_ALREADY_USED)
                        } else if (err.contains(usernameErr)) {
                            authErrorMessage = ErrorMessage(MESSAGE.SIGNUP_USERNAME_ALREADY_USED)
                        } else {
                            authErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        }

                        it.resume(null)
                        return@signup
                    }

                    val id = res.Signup()!!.id()!!
                    val followers = res.Signup()!!.followers() as ArrayList<String>
                    val following = res.Signup()!!.following() as ArrayList<String>

                    val user = UserType(
                        id,
                        username,
                        screenName,
                        "null",
                        followers,
                        following,
                        ArrayList()
                    )

                    cacheSignupInfo(res, screenName)

                    it.resume(user)
                })
        }

    suspend fun updateFirebaseToken(newToken: String): Boolean =
        suspendCoroutine {
            Mutations(context).updateFirebaseToken(newToken,
                onCompleted = { err, res ->
                    if (err != null || res == null || res.updateFirebaseToken() == null) {
                        it.resume(false)
                        updateTokenError = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                        return@updateFirebaseToken
                    }

                    if (!res.updateFirebaseToken()!!) {
                        updateTokenError = ErrorMessage(MESSAGE.FIREBASE_TOKEN_ERROR)
                        return@updateFirebaseToken
                    }

                    it.resume(res.updateFirebaseToken()!!)
                })
        }

    suspend fun logout(): Boolean =
        suspendCoroutine {
            Mutations(context).logout(onCompleted = { err, res ->
                if (err != null || res == null || res.Logout() == null) {
                    logoutErrorMessage = ErrorMessage(MESSAGE.CONNECTION_ERROR)
                    it.resume(false)
                    return@logout
                }

                if (!res.Logout()!!) {
                    logoutErrorMessage = ErrorMessage(MESSAGE.LOGOUT_ERROR)
                    return@logout
                }

                CacheHandler(context).deleteCacheDir()
                it.resume(res.Logout()!!)
            })
        }

    private fun cacheLoginInfo(res: LoginMutation.Data) {
        val id = res.Login()!!.id()!!
        val uName = res.Login()!!.username()!!
        val screenName = res.Login()!!.screenName()!!
        val avatar = res.Login()!!.avatar()!!
        val jwt = res.Login()!!.jwt()!!
        val posts = res.Login()!!.posts()!!
        val followers = res.Login()!!.followers() as List<String>
        val following = res.Login()!!.following() as List<String>

        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        jsonObject.put("username", uName)
        jsonObject.put("screenName", screenName)
        jsonObject.put("avatar", avatar)
        jsonObject.put("jwt", jwt)
        jsonObject.put("followers", JSONArray(followers))
        jsonObject.put("following", JSONArray(following))
        jsonObject.put("posts", JSONArray(posts))

        CacheHandler(context).storeLoginCache(jsonObject)
    }

    private fun cacheSignupInfo(res: SignupMutation.Data, screenName: String) {
        val id = res.Signup()!!.id()
        val uName = res.Signup()!!.username()
        val jwt = res.Signup()!!.jwt()
        val followers = res.Signup()!!.followers() as List<String>
        val following = res.Signup()!!.following() as List<String>

        // store them in cache
        val jsonObject = JSONObject()
        jsonObject.put("id", id)
        jsonObject.put("username", uName)
        jsonObject.put("screenName", screenName)
        jsonObject.put("jwt", jwt)
        jsonObject.put("followers", JSONArray(followers))
        jsonObject.put("following", JSONArray(following))
        jsonObject.put("posts", JSONArray())

        CacheHandler(context).storeLoginCache(jsonObject)
    }
}
