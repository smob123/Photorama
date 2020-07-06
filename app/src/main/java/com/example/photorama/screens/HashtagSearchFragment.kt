package com.example.photorama.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.photorama.R
import com.example.photorama.adapters.HashtagListAdapter
import com.example.photorama.viewModels.SearchViewModel
import com.example.photorama.viewModels.SearchViewModelFactory
import kotlinx.android.synthetic.main.user_search_fragment.*

/**
 * @author Sultan
 * displays a list of hashtags that match the user's search term
 */

class HashtagSearchFragment : Fragment() {

    lateinit var viewModel: SearchViewModel
    lateinit var arrayAdapter: HashtagListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.hashtag_search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = SearchViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(requireActivity(), factory).get(SearchViewModel::class.java)

        initAdapter()
        initHashtagSearchObserver()
        initHashtagSearchErrorObserver()

        val container = view.parent as ViewGroup
        setOnChangeListener(container.rootView)
    }

    fun initAdapter() {
        arrayAdapter = HashtagListAdapter(
            this@HashtagSearchFragment.activity!!.applicationContext,
            ArrayList()
        )

        search_results.adapter = arrayAdapter
        setAdapterOnClickListener()
    }

    fun initHashtagSearchObserver() {
        viewModel.getHashtags().observe(requireActivity(), Observer { hashtags ->
            arrayAdapter.setValues(hashtags)
            arrayAdapter.notifyDataSetChanged()
        })
    }

    private fun initHashtagSearchErrorObserver() {
        viewModel.getHashtagSearchErrorMessage().observe(requireActivity(), Observer { error ->
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
                    viewModel.searchHashtags(text)
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
     */
    private fun setAdapterOnClickListener() {
        search_results.setOnItemClickListener { _, _, position, _ ->
            // get the clicked hashtag's name, and pass it to the hashtag results fragment as args
            val item = arrayAdapter.getItem(position)
            val fragment = HashtagPostsFragment()
            val args = Bundle()
            args.putString("hashtag", item!!.hashtag)
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
