package com.example.photorama

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.photorama.adapters.UserListAdapter
import com.example.photorama.viewModels.SearchViewModel
import com.example.photorama.viewModels.SearchViewModelFactory
import kotlinx.android.synthetic.main.activity_user_list.*

/**
 * @author Sultan.
 * Displays a list of users, who are either following or followed by the user.
 */

class UserListActivity : AppCompatActivity() {

    private lateinit var arrayAdapter: UserListAdapter
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // display a back button to the action bar, and set its title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Users"

        // check if a non-empty list of user IDs was passed as an intent
        if (intent.extras == null || intent.extras!!.getStringArrayList("userIds") == null
            || intent.extras!!.getStringArrayList("userIds").isEmpty()
        ) {
            displayMessage()
            return
        }

        val factory = SearchViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(SearchViewModel::class.java)
        initAdapter()
        initUserSearchObserver()
        initUserSearchErrorObserver()

        // get the list of user IDs to fetch their data from the server
        val usersIds = intent.extras!!.getStringArrayList("userIds")
        viewModel.searchUsersById(usersIds as ArrayList<String>)
    }

    private fun initAdapter() {
        arrayAdapter = UserListAdapter(
            this@UserListActivity,
            ArrayList()
        )
        user_list.adapter = arrayAdapter

        // set the onClick listener for the search list's custom adapter
        user_list.setOnItemClickListener { _, _, position, _ ->
            val item = arrayAdapter.getItem(position)
            val intent = Intent(this@UserListActivity, SearchActivity::class.java)
            intent.putExtra("username", item!!.username)
            startActivity(intent)
        }
    }

    private fun initUserSearchObserver() {
        viewModel.getUsersSearchedById().observe(this, Observer { users ->
            arrayAdapter.setValues(users)
        })
    }

    private fun initUserSearchErrorObserver() {
        viewModel.getUserSearchErrorMessage().observe(this, Observer { error ->
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
        })
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
}
