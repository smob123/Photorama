package com.example.photorama

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.screens.LoginFragment

/**
 * @author Sultan
 * Handles signing in.
 */
class LoginActivity : AppCompatActivity() {

    private val loginFragment = LoginFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.hide()
        // check if the user needs to login
        checkIfCanLogIn()
    }

    /**
     * checks if the user needs to login or not.
     */
    private fun checkIfCanLogIn() {
        // get the app's cache
        val jsonObject = CacheHandler(this@LoginActivity.applicationContext)
            .getCache()

        // check that the cache doesn't contain the required authentication information
        if (!jsonObject.has("username") || !jsonObject.has("id") || !jsonObject.has("jwt")) {
            displayActivity()
            return
        }

        // otherwise move on to the next activity
        val intent = Intent(
            this@LoginActivity.applicationContext,
            MainAppActivity::class.java
        )
        this@LoginActivity.startActivity(intent)
    }

    /**
     * changes the activity's theme, and displays the login screen.
     */
    private fun displayActivity() {
        // set the content view, and change the theme to display the login screen
        setContentView(R.layout.activity_login)
        setTheme(R.style.AppTheme)

        supportFragmentManager.beginTransaction()
            .replace(R.id.form, loginFragment)
            .commit()
    }
}
