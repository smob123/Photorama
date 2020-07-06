package com.example.photorama

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main_app.*
import com.example.photorama.screens.ExploreFragment
import com.example.photorama.screens.HomeFragment
import com.example.photorama.screens.NotificationsFragment
import com.example.photorama.screens.ProfileFragment

/**
 * @author Sultan
 * responsible for handling what is being displayed on the app's main screen.
 */
class MainAppActivity : AppCompatActivity() {

    // the app's main com.example.photorama.screens
    private lateinit var homeFragment: HomeFragment
    private lateinit var exploreFragment: ExploreFragment
    private lateinit var notificationsFragment: NotificationsFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_app)

        // get storage permission
        getStoragePermission()

        // add home fragment, and set it to be the active fragment
        homeFragment = HomeFragment()
        if(supportFragmentManager.fragments.isEmpty()) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.main_frame, homeFragment, "home")
                .commit()
        }

        activeFragment = homeFragment

        // initialize the bottom nav bar's listener
        initBottomNavListener()
    }

    /**
     * requires storage permission in order to be able to store, and access images.
     */
    private fun getStoragePermission() {
        // check if permissions were granted
        val permission = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!checkForPermission(permission)) {
            ActivityCompat.requestPermissions(this, permission, 0)
        }
    }

    /**
     * checks if storage permission was granted
     * @return if the permissions was granted or not
     */
    private fun checkForPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // check if the permission was denied
        for (result in grantResults) {
            // then display a message explaining why the the permission is needed
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permission is required for the app's functionality",
                    Toast.LENGTH_LONG
                ).show()

                // request the permission again
                val permission = arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                checkForPermission(permission)
            }
        }
    }

    /**
     * initializes the bottom navigation bar's listener.
     */
    private fun initBottomNavListener() {
        bottom_nav_bar.setOnNavigationItemSelectedListener { menuItem ->
            // check which item is selected, and lazy initialize the corresponding screen
            when (menuItem.itemId) {

                R.id.home_tab -> {
                    displayFragment(homeFragment)

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.explore_tab -> {
                    if (::exploreFragment.isInitialized) {
                        displayFragment(exploreFragment)
                    } else {
                        exploreFragment = ExploreFragment()
                        addFragment(exploreFragment, "explore")
                    }

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.notifications_tab -> {
                    if (::notificationsFragment.isInitialized) {
                        displayFragment(notificationsFragment)
                    } else {
                        notificationsFragment = NotificationsFragment()
                        addFragment(notificationsFragment, "notifications")
                    }

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.profile_tab -> {
                    if (::profileFragment.isInitialized) {
                        displayFragment(profileFragment)
                    } else {
                        profileFragment = ProfileFragment()
                        addFragment(profileFragment, "profile")
                    }

                    return@setOnNavigationItemSelectedListener true
                }
            }

            return@setOnNavigationItemSelectedListener false
        }

        initFabListener()
    }

    /**
     * adds new fragments to the fragment manager.
     * @param fragment the new fragment to add
     * @param fragmentTag the unique tag of the new fragment
     */
    private fun addFragment(fragment: Fragment, fragmentTag: String) {
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .add(R.id.main_frame, fragment, fragmentTag)
            .commit()

        activeFragment = fragment
    }

    /**
     * shows/hides fragments on the main frame.
     * @param fragment the fragment we want to display
     */
    private fun displayFragment(fragment: Fragment) {
        if (activeFragment == fragment) {
            return
        }

        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()

        activeFragment = fragment
    }

    /**
     * initializes the floating action buttons listener to open an ImageSelectionActivity.
     */
    private fun initFabListener() {
        new_post_btn.setOnClickListener {
            val intent = Intent(this, ImageSelectionActivity::class.java)
            intent.putExtra("image type", ImageSelectionActivity.ImageType.POST)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // hide the activity in the background
        moveTaskToBack(true)
    }
}
