package com.example.photorama

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.photorama.screens.HashtagPostsFragment
import com.example.photorama.screens.ProfileFragment
import com.example.photorama.screens.SearchFragment

/**
 * @author Sultan
 * handles searching for content withing the app.
 */

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // add a back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // check if a username was passed as an extra
        val username = intent.getStringExtra("username")

        // check if a hashtag was passed as an extra
        val hashtag = intent.getStringExtra("hashtag")

        if (username != null) {
            val profileFragment = ProfileFragment()
            val args = Bundle()
            args.putString("username", username)
            profileFragment.arguments = args

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_frame, profileFragment)
                .commit()
        } else if (hashtag != null) {
            val hashtagFragment = HashtagPostsFragment()
            val args = Bundle()
            args.putString("hashtag", hashtag)
            hashtagFragment.arguments = args
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_frame, hashtagFragment)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.search_frame, SearchFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // set the action bar's back button on click listener
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return false
    }

    override fun onBackPressed() {
        // check if this is the root activity
        if (isTaskRoot) {
            // go back to the main app's activity
            val intent = Intent(this, MainAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            // otherwise call the default method
            super.onBackPressed()
        }
    }
}
