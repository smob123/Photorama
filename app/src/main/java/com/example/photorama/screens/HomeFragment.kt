package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.R
import com.example.photorama.adapters.TimelinePostAdapter
import com.example.photorama.viewModels.TimelineViewModel
import com.example.photorama.viewModels.TimelineViewModelFactory
import kotlinx.android.synthetic.main.home_fragment.*

/**
 * @author Sultan
 * timeline screen
 */

class HomeFragment : Fragment() {
    // the range of posts to fetch from the server
    private var postListRange = IntRange(0, 5)

    // the amount of increment to increase the range by, in order to fetch more posts
    private val rangeIncrement = 5
    private lateinit var timelineViewModel: TimelineViewModel
    private lateinit var timelinePostAdapter: TimelinePostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // initialize the view model
        val factory = TimelineViewModelFactory(requireActivity())
        timelineViewModel =
            ViewModelProvider(requireActivity(), factory).get(TimelineViewModel::class.java)

        // initialize UI elements
        initRecyclerView()
        initLoadingViewModel()
        initPostViewModel()
        initPostDeletionObserver()
        initNetworkErrorObserver()

        // set refresh layout listener, and the on scroll listener
        setRefreshLayoutListener()
        // fetch the first 5 posts in the timeline
        timelineViewModel.fetchTimelinePosts(postListRange.first, postListRange.last, true)
    }

    /**
     * initializes the loading state observer.
     */
    private fun initLoadingViewModel() {
        timelineViewModel.isFetching().observe(requireActivity(), Observer { isFetching ->
            refresh_layout.isRefreshing = isFetching!!
        })
    }

    /**
     * initializes the timeline posts' observer.
     */
    private fun initPostViewModel() {
        timelineViewModel.getTimelinePosts()
            .observe(requireActivity(), Observer { newPosts ->
                // set, or add to the recycler view's adapter based on the post range
                if (postListRange.first > 0) {
                    // only add posts that haven't been added before
                    for (newPost in newPosts) {
                        var postExists = false
                        for (existingPost in timelinePostAdapter.getItems()) {
                            if (newPost.id == existingPost.id) {
                                postExists = true
                                break
                            }
                        }
                        if (!postExists) {
                            timelinePostAdapter.addItem(newPost)
                        }
                    }
                } else {
                    timelinePostAdapter.setItems(newPosts)
                }

                timelinePostAdapter.notifyDataSetChanged()

                // set the visibility of the recycler view, and the message text view
                if (timelinePostAdapter.itemCount > 0) {
                    timeline.visibility = View.VISIBLE
                    no_posts_text.visibility = View.GONE
                } else {
                    timeline.visibility = View.GONE
                    no_posts_text.visibility = View.VISIBLE
                }
            }
            )
    }

    /**
     * removes posts that were deleted by the user from the timeline.
     */
    private fun initPostDeletionObserver() {
        timelineViewModel.getDeletedPostId().observe(requireActivity(), Observer { postId ->
            for (i in 0 until timelinePostAdapter.itemCount) {
                val post = timelinePostAdapter.getItems()[i]
                if (post.id == postId) {
                    timelinePostAdapter.removeItem(i)
                    break
                }
            }

            timelinePostAdapter.notifyDataSetChanged()
        })
    }

    /**
     * observes network operation failure.
     */
    private fun initNetworkErrorObserver() {
        timelineViewModel.getErrorMessage().observe(requireActivity(), Observer { error ->
            Toast.makeText(requireActivity(), error.getMessage(), Toast.LENGTH_LONG).show()
        })
    }

    /**
     * initializes the recycler view to display the user's timeline.
     */
    private fun initRecyclerView() {
        // initialize the recycler view's layout manager, and adapter
        timeline.layoutManager =
            LinearLayoutManager(requireActivity().applicationContext)

        timelinePostAdapter =
            TimelinePostAdapter(
                requireActivity(),
                ArrayList()
            )

        timeline.adapter = timelinePostAdapter
        setScrollListener()
    }

    /**
     * set the refresh layout's on refresh listener.
     */
    private fun setRefreshLayoutListener() {
        refresh_layout.setOnRefreshListener {
            // reset the range to fetch the latest 5 posts
            postListRange = IntRange(0, 5)
            // fetch the first 5 posts in the timeline
            timelineViewModel.fetchTimelinePosts(postListRange.first, postListRange.last, false)
            // set the scroll listener
            setScrollListener()
        }
    }

    /**
     * sets the on scroll listener for the recycler view.
     */
    private fun setScrollListener() {
        timeline.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isScrolling = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val manager = timeline.layoutManager as LinearLayoutManager
                val visibleItems = manager.childCount
                val totalNumOfItems = manager.itemCount
                val scrolledOutItems = manager.findFirstVisibleItemPosition()

                // check if the user has scrolled to the bottom of the list
                if (isScrolling && (visibleItems + scrolledOutItems) == totalNumOfItems) {
                    isScrolling = false
                    // increase the range, and get more posts
                    postListRange = IntRange(
                        postListRange.first + rangeIncrement,
                        postListRange.last + rangeIncrement
                    )

                    // fetch the first 5 posts in the timeline
                    timelineViewModel.fetchTimelinePosts(
                        postListRange.first,
                        postListRange.last,
                        false
                    )
                }
            }
        })
    }
}
