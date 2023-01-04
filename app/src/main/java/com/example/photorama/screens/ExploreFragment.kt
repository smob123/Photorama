package com.example.photorama.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.R
import com.example.photorama.SearchActivity
import com.example.photorama.adapters.GridViewAdapter
import com.example.photorama.viewModels.ExploreViewModel
import com.example.photorama.viewModels.ExploreViewModelFactory
import kotlinx.android.synthetic.main.explore_fragment.*

/**
 * @author Sultan
 * explore posts fragment.
 */

class ExploreFragment : Fragment() {
    // range of posts to fetch from the network
    private var postRange = IntRange(0, 20)

    // the amount of increment to increase the range by, in order to fetch more comments
    private val rangeIncrement = 20
    private lateinit var postViewModel: ExploreViewModel
    private lateinit var postAdapter: GridViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = ExploreViewModelFactory(requireActivity())
        postViewModel =
            ViewModelProvider(requireActivity(), factory).get(ExploreViewModel::class.java)

        // initialize observers
        initLoadingViewModel()
        initErrorMessageObserver()
        // initialize UI elements
        initRecyclerView()
        initPostViewModel()
        // set listeners
        setRefreshLayoutListener()
        setSearchBarClickListener()
        addOnScrollListener()
        // fetch the first group of posts
        postViewModel.fetchRecommendedPosts(postRange.first, postRange.last)
    }

    /**
     * initializes the loading state observer.
     */
    private fun initLoadingViewModel() {
        postViewModel.isFetching().observe(requireActivity(), Observer { isFetching ->
            refresh_layout.isRefreshing = isFetching!!
        })
    }

    private fun initErrorMessageObserver() {
        postViewModel.getPostRecommendationErrorMessage()
            .observe(requireActivity(), Observer { error ->
                Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show()
            })
    }

    /**
     * initializes the recommended posts' observer.
     */
    private fun initPostViewModel() {
        postViewModel.getRecommendedPosts()
            .observe(requireActivity(), Observer { posts ->
                if (postRange.first > 0) {
                    postAdapter.addItems(posts)
                } else {
                    postAdapter.setItems(posts)
                }

                postAdapter.notifyDataSetChanged()
            }
            )
    }

    /**
     * initializes the recycler view to display the user's recommended posts.
     */
    private fun initRecyclerView() {
        // initialize the adapter
        postAdapter = GridViewAdapter(
            this@ExploreFragment.activity!!,
            ArrayList()
        )

        // initialize the manager
        val manager = GridLayoutManager(
            this@ExploreFragment.activity!!,
            3,
            RecyclerView.VERTICAL,
            false
        )

        // add them to the recycler view
        explore_posts.layoutManager = manager
        explore_posts.adapter = postAdapter
    }

    /**
     * set the refresh layout's on refresh listener.
     */
    private fun setRefreshLayoutListener() {
        refresh_layout.setOnRefreshListener {
            // reset the post range to 20 new posts
            postRange = IntRange(0, 20)
            // fetch the first group of posts
            postViewModel.fetchRecommendedPosts(postRange.first, postRange.last)
        }
    }

    /**
     * sets the search bar's on click change.
     */
    private fun setSearchBarClickListener() {
        search_bar.setOnClickListener {
            val activity = this@ExploreFragment.activity!!
            val intent = Intent(activity.applicationContext, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * adds an on scroll listener to the recycler view.
     */
    private fun addOnScrollListener() {
        explore_posts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var isScrolling = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val manager = explore_posts.layoutManager as LinearLayoutManager
                val visibleItems = manager.childCount
                val totalNumOfItems = manager.itemCount
                val scrolledOutItems = manager.findFirstVisibleItemPosition()

                // check if the user has scrolled to the end
                if (isScrolling && (visibleItems + scrolledOutItems) == totalNumOfItems) {
                    isScrolling = false
                    // increase the range, and get more posts from the server
                    postRange = IntRange(
                        postRange.first + rangeIncrement,
                        postRange.last + rangeIncrement
                    )

                    postViewModel.fetchRecommendedPosts(postRange.first, postRange.last)
                }
            }
        })
    }
}
