package com.example.photorama.screens

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.photorama.R
import com.example.photorama.adapters.UserListAdapter
import com.example.photorama.heplerObjects.UserListItemType
import com.example.photorama.viewModels.SearchViewModel
import com.example.photorama.viewModels.SearchViewModelFactory
import kotlinx.android.synthetic.main.user_search_fragment.*

/**
 * @author Sultan
 * displays a list of users that match the user's search term
 */

class UserSearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    lateinit var arrayAdapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = SearchViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(requireActivity(), factory).get(SearchViewModel::class.java)
        initAdapter()
        initUserSearchObserver()
        initUserSearchErrorObserver()

        val container = view.parent as ViewGroup

        setOnChangeListener(container.rootView)
    }

    fun initAdapter() {
        arrayAdapter = UserListAdapter(requireActivity(), ArrayList())

        search_results.adapter = arrayAdapter
        setAdapterOnClickListener()
    }

    fun initUserSearchObserver() {
        viewModel.getUsers().observe(requireActivity(), Observer { users ->
            val usersList = ArrayList<UserListItemType>()

            for (user in users) {
                val u =
                    UserListItemType(user.userId, user.username, user.screenName, user.avatar)
                usersList.add(u)
            }

            arrayAdapter.setValues(usersList)
            arrayAdapter.notifyDataSetChanged()
        })
    }

    private fun initUserSearchErrorObserver() {
        viewModel.getUserSearchErrorMessage().observe(requireActivity(), Observer { error ->
            Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show()
        })
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
                    viewModel.searchUsers(text)
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
     * sets the onClick listener for the search list's custom adapter.
     * @param adapter the custom adapter to add the listener to
     */
    private fun setAdapterOnClickListener() {
        search_results.setOnItemClickListener { _, _, position, _ ->
            // get the clicked item's username, and pass it to the profile fragment as args
            val item = arrayAdapter.getItem(position)
            val profileFragment = ProfileFragment()
            val args = Bundle()
            args.putString("username", item!!.username)
            profileFragment.arguments = args

            // replace the current fragment with the user fragment
            val activity = requireActivity() as AppCompatActivity

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
