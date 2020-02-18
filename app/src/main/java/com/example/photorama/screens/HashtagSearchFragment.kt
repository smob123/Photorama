package com.example.photorama.screens

import com.example.photorama.adapters.HashtagListAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.photorama.R
import com.example.photorama.SearchHashtagsByNameQuery
import kotlinx.android.synthetic.main.user_search_fragment.*
import com.example.photorama.networking.Queries

/**
 * @author Sultan
 * displays a list of hashtags that match the user's search term
 */

class HashtagSearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.hashtag_search_fragment, container, false)
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
                    Queries(this@HashtagSearchFragment.activity!!).searchHashtags(
                        text,
                        onCompleted = { err, res ->
                            if (err != null) {
                                // display an error message if an error occurs
                                this@HashtagSearchFragment.activity?.runOnUiThread {
                                    Toast.makeText(
                                        this@HashtagSearchFragment.activity!!.applicationContext,
                                        "Couldn't fetch data from network",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                return@searchHashtags
                            }

                            if (res != null) {
                                if(res.searchHashtagsByName() == null) {
                                    // display an error message if an error occurs
                                    this@HashtagSearchFragment.activity?.runOnUiThread {
                                        Toast.makeText(
                                            this@HashtagSearchFragment.activity!!.applicationContext,
                                            "Couldn't fetch data from network",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    return@searchHashtags
                                }
                                val hashtags = res.searchHashtagsByName()!!
                                updateUI(hashtags)
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
    private fun updateUI(hashtags: List<SearchHashtagsByNameQuery.SearchHashtagsByName>) {
        val adapter = HashtagListAdapter(
            this@HashtagSearchFragment.activity!!.applicationContext,
            hashtags
        )

        this@HashtagSearchFragment.activity!!.runOnUiThread {
            search_results.adapter = adapter
        }

        setAdapterOnClickListener(adapter)
    }

    /**
     * sets the onClick listener for the search list's custom adapter.
     * @param adapter the custom adapter to add the listener to
     */
    private fun setAdapterOnClickListener(adapter: HashtagListAdapter) {
        search_results.setOnItemClickListener { _, _, position, _ ->
            // get the clicked hashtag's name, and pass it to the hashtag results fragment as args
            val item = adapter.getItem(position)
            val fragment = HashtagPostsFragment()
            val args = Bundle()
            args.putString("hashtag", item!!.hashtag().toString())
            fragment.arguments = args

            // replace the current fragment with the hashtags results fragment
            this@HashtagSearchFragment.activity!!
                .supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(
                    R.id.search_frame,
                    fragment
                )
                .commit()
        }
    }
}
