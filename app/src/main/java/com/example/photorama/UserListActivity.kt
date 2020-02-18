package com.example.photorama

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.photorama.adapters.UserListAdapter
import com.example.photorama.heplerObjects.UserListItemType
import kotlinx.android.synthetic.main.activity_user_list.*
import com.example.photorama.networking.Queries

/**
 * @author Sultan.
 * Displays a list of users, who are either following or followed by the user.
 */

class UserListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // display a back button to the action bar, and set its title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Users"

        // check if a non-empty list of user IDs was passed as an intent
        if (intent.extras == null || intent.extras!!.getStringArrayList("userIds") == null
            || intent.extras!!.getStringArrayList("userIds").isEmpty()) {
            displayMessage()
            return
        }

        // get the list of user IDs to fetch their data from the server
        val usersIds = intent.extras!!.getStringArrayList("userIds")
        getUsers(usersIds!!)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // set the action bar's back button's on click listener
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * display a text view telling the user that there is nothing to show here.
     */
    private fun displayMessage() {
        no_users_text.visibility = View.VISIBLE
        user_list.visibility = View.GONE
    }

    /**
     * fetch the users from the server.
     * @param userIds the IDs of the users we want to display on this activity
     */
    private fun getUsers(userIds: ArrayList<String>) {
        Queries(this@UserListActivity.applicationContext)
            .getUsersByIds(userIds,
                onCompleted = { err, res ->
                    if (err != null) {
                        return@getUsersByIds
                    }

                    if (res != null) {
                        if (res.getUsersByIds != null && res.getUsersByIds.isNotEmpty()) {
                            displayUsers(res.getUsersByIds)
                        }
                    }
                })
    }

    /**
     * displays the list of users.
     * @param users the list of users that we got from the server
     */
    private fun displayUsers(users: List<GetUsersByIdsQuery.GetUsersById>) {
        // create an array list of user objects to pass it to the list view's adapter
        val usersList = ArrayList<UserListItemType>()

        for (item in users) {
            val user = UserListItemType(
                item.id()!!,
                item.username()!!,
                item.screenName()!!,
                item.avatar()!!
            )
            usersList.add(user)
        }

        this@UserListActivity.runOnUiThread {
            val adapter = UserListAdapter(
                this@UserListActivity,
                usersList
            )
            user_list.adapter = adapter

            setAdapterOnClickListener(adapter)
        }
    }

    /**
     * sets the onClick listener for the search list's custom adapter.
     * @param adapter the custom adapter to add the listener to
     */
    private fun setAdapterOnClickListener(adapter: UserListAdapter) {
        user_list.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position)
            val intent = Intent(this@UserListActivity, SearchActivity::class.java)
            intent.putExtra("username", item!!.username)
            startActivity(intent)
        }
    }
}
