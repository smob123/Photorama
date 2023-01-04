package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photorama.R
import com.example.photorama.adapters.GridViewAdapter
import com.example.photorama.viewModels.SearchViewModel
import com.example.photorama.viewModels.SearchViewModelFactory
import kotlinx.android.synthetic.main.hashtag_results_fragment.*

/**
 * @author Sultan
 * displays all the posts in a hashtag.
 */

class HashtagPostsFragment : Fragment() {

    private var hashtag = ""
    private lateinit var viewModel: SearchViewModel
    private lateinit var arrayAdapter: GridViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.hashtag_results_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // check if there are no arguments passed
        if (arguments == null || arguments?.getString("hashtag") == null) {
            this@HashtagPostsFragment.activity!!.onBackPressed()
        }

        // otherwise get the passed hashtag argument
        hashtag = arguments!!.getString("hashtag")!!

        // display the action bar, and give it a title
        val actionBar = (this@HashtagPostsFragment.activity as AppCompatActivity)
            .supportActionBar

        actionBar?.show()
        actionBar?.title = hashtag

        // initialize the array adapter
        initAdapter()

        // initialize the view model
        val factory = SearchViewModelFactory(requireActivity())
        viewModel = ViewModelProvider(requireActivity(), factory).get(SearchViewModel::class.java)
        viewModel.searchPostsByHashtag(hashtag)

        // initialize observers
        initPostsObserver()
        initPostSearchErrorObserver()
    }

    private fun initAdapter() {
        arrayAdapter = GridViewAdapter(requireActivity(), ArrayList())
        val manager = GridLayoutManager(
            requireActivity(),
            3,
            GridLayoutManager.VERTICAL,
            false
        )

        hashtag_posts.layoutManager = manager
        hashtag_posts.adapter = arrayAdapter
    }

    private fun initPostsObserver() {
        viewModel.getPosts().observe(requireActivity(), Observer { posts ->
            arrayAdapter.setItems(posts)
            arrayAdapter.notifyDataSetChanged()
        })
    }

    private fun initPostSearchErrorObserver() {
        viewModel.getPostSearchErrorMessage().observe(requireActivity(), Observer { error ->
            Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show()
        })
    }
}
