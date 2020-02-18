package com.example.photorama.screens

import com.example.photorama.adapters.UserListAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.photorama.R
import com.example.photorama.SearchUsersByNameQuery
import com.example.photorama.heplerObjects.UserListItemType
import kotlinx.android.synthetic.main.user_search_fragment.*
import com.example.photorama.networking.Queries

/**
 * @author Sultan
 * displays a list of users that match the user's search term
 */

class UserSearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.parent as ViewGroup

        setOnChangeListener(container.rootView)
    }

    /**
     * sends a request to the server to get users that match the search term.
     * @param container the parent view that contains the EditText element to add the listener to
     */
    private fun setOnChangeListener(container: View) {
        val search_bar = container.findViewById<EditText>(R.id.search_bar)

        search_bar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val text = search_bar.text.toString()

                if (text.isNotEmpty() && !text.startsWith(" ")) {
                    Queries(this@UserSearchFragment.activity!!).searchUserByName(
                        text,
                        onCompleted = { err, res ->
                            if (err != null) {
                                // display an error message if an error occurs
                                this@UserSearchFragment.activity?.runOnUiThread {
                                    Toast.makeText(
                                        this@UserSearchFragment.activity!!.applicationContext,
                                        "Couldn't fetch data from network",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                return@searchUserByName
                            }

                            if (res != null) {
                                if (res.searchUsersByName() == null) {
                                    // display an error message if an error occurs
                                    this@UserSearchFragment.activity?.runOnUiThread {
                                        Toast.makeText(
                                            this@UserSearchFragment.activity!!.applicationContext,
                                            "Couldn't fetch data from network",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    return@searchUserByName
                                }

                                this@UserSearchFragment.activity!!.runOnUiThread {
                                    updateUI(res.searchUsersByName()!!)
                                }
                            }
                        })
                }
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }

    /**
     * adds an adapter to the list view, in order to update the ui to display the search results.
     * @param users the list of user accounts fetched from the server
     */
    private fun updateUI(users: List<SearchUsersByNameQuery.SearchUsersByName>) {
        val usersList = ArrayList<UserListItemType>()

        for (item in users) {
            val user = UserListItemType(
                item.id()!!,
                item.username()!!,
                item.screenName()!!,
                item.avatar().toString()
            )
            usersList.add(user)
        }

        val searchAdapter =
            UserListAdapter(
                this@UserSearchFragment.activity!!.applicationContext,
                usersList
            )
        search_results.adapter = searchAdapter

        setAdapterOnClickListener(searchAdapter)
    }

    /**
     * sets the onClick listener for the search list's custom adapter.
     * @param adapter the custom adapter to add the listener to
     */
    private fun setAdapterOnClickListener(adapter: UserListAdapter) {
        search_results.setOnItemClickListener { _, _, position, _ ->
            // get the clicked item's username, and pass it to the profile fragment as args
            val item = adapter.getItem(position)
            val profileFragment = ProfileFragment()
            val args = Bundle()
            args.putString("username", item!!.username)
            profileFragment.arguments = args

            // replace the current fragment with the user fragment
            val activity = this@UserSearchFragment.activity as AppCompatActivity

            activity
                .supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(
                    R.id.search_frame,
                    profileFragment
                )
                .commit()

            // display the action bar, and set its title to display the user's username
            activity.supportActionBar?.show()
            activity.supportActionBar?.title = item.username
        }
    }
}
