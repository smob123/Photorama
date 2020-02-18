package com.example.photorama

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.photorama.heplerObjects.CacheHandler
import kotlinx.android.synthetic.main.activity_settings.*
import com.example.photorama.networking.Mutations

/**
 * @author Sultan
 * displays, and handles user settings
 */

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // add a back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // add settings list to UI
        addListItems()
        // set the on click listener for those settings
        setListOnClickListener()
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

    /**
     * adds items to the settings list.
     */
    private fun addListItems() {
        // add a logout option
        val items = arrayOf("Logout")

        val adapter =
            ArrayAdapter<String>(this@SettingsActivity, android.R.layout.simple_list_item_1, items)
        settings_list.adapter = adapter
    }

    /**
     * sets the on click listener for the items in the settings list.
     */
    private fun setListOnClickListener() {
        // since there is only one item, which is the logout option, ask the user to confirm if
        // they want to logout
        settings_list.setOnItemClickListener { _, _, _, _ ->
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    logout()
                })
                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                .show()
        }
    }

    /**
     * sends a logout request to the server, and removes the app's cache.
     */
    private fun logout() {
        Mutations(this@SettingsActivity.applicationContext).logout(onCompleted = { err, res -> })
        CacheHandler(this@SettingsActivity).deleteCacheDir()
        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}
