package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.GetUserTimelineQuery
import com.example.photorama.R
import com.example.photorama.adapters.TimelinePostAdapter
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.Queries
import kotlinx.android.synthetic.main.home_fragment.*

/**
 * @author Sultan
 * home fragment
 */

class HomeFragment : Fragment() {

    // the range of posts to fetch from the server
    private var postListRange = IntRange(0, 5)
    // the amount of increment to increase the range by, in order to fetch more posts
    private val RANGE_INCREMENT = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getTimeline()
        setRefreshLayoutListener()
        setScrollListener()
    }

    /**
     * set the refresh layout's on refresh listener.
     */
    private fun setRefreshLayoutListener() {
        refresh_layout.setOnRefreshListener {
            // reset the range to fetch the latest 5 posts
            postListRange = IntRange(0, 5)
            getTimeline()
        }
    }

    private fun getTimeline() {
        Queries(activity!!).getTimeLine(
            postListRange.first, postListRange.last,
            onCompleted = { err, res ->
                // stop the refresh layout from refreshing
                this@HomeFragment.activity?.runOnUiThread {
                    if (refresh_layout.isRefreshing) {
                        refresh_layout.isRefreshing = false
                    }
                }

                if (err != null) {
                    this@HomeFragment.activity?.runOnUiThread {
                        Toast.makeText(
                            this@HomeFragment.activity!!.applicationContext,
                            "Couldn't fetch posts from the network",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@getTimeLine
                }

                if (res != null) {
                    val adapter = timeline?.adapter
                    // check if the result is null
                    if (res.userTimeline == null) {
                        this@HomeFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@HomeFragment.activity!!.applicationContext,
                                "Couldn't fetch posts from the network",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@getTimeLine
                    }

                    // check if the result empty, and the adapter hasn't been initialized
                    if (res.userTimeline!!.isEmpty() && adapter == null) {
                        // display the text view to explain what this screen is for
                        displayMessage()

                        // otherwise if the range starts from 0, or the result isn't empty and the
                        // adapter hasn't been initialized
                    } else if (postListRange.first == 0 || (res.userTimeline!!.isNotEmpty() && adapter == null)) {
                        // add an adapter to the recycler view, and display the posts
                        displayPosts(res.userTimeline!!)

                        // otherwise, if the result is empty but the adapter has been initialized
                    } else if ((res.userTimeline!!.isEmpty())
                        && adapter != null
                    ) {
                        // remove the scroll listener from the recycler view
                        this@HomeFragment.activity?.runOnUiThread {
                            timeline.clearOnScrollListeners()
                        }

                        // otherwise, if the result isn't empty but the adapter has been initialized
                    } else if (res.userTimeline!!.isNotEmpty() && adapter != null) {
                        // add new items to the adapter
                        addTimelineElements(res.userTimeline!!)
                    }
                }
            }
        )
    }

    /**
     * initializes the recycler view's adapter, and displays posts.
     * @param posts list of posts that will be shown
     */
    private fun displayPosts(posts: List<GetUserTimelineQuery.GetUserTimeline>) {
        // store posts' data into an array list of PostType
        val timelinePosts = ArrayList<PostType>()
        for (post in posts) {
            val postType = PostType(
                post.id().toString(),
                post.userId(),
                post.username(),
                post.userScreenName(),
                post.userAvatar(),
                post.image(),
                post.likes() as List<String>,
                post.comments() as List<String>,
                post.datetime(),
                post.description().toString()
            )

            timelinePosts.add(postType)
        }

        this@HomeFragment.activity?.runOnUiThread {
            // set the visibility of the recycler view, and the message text view
            timeline.visibility = View.VISIBLE
            no_posts_text.visibility = View.GONE

            // initialize the recycler view's layout manager, and adapter
            timeline.layoutManager =
                LinearLayoutManager(this@HomeFragment.activity!!.applicationContext)

            timeline.adapter =
                TimelinePostAdapter(
                    this@HomeFragment.activity!!,
                    timelinePosts
                )
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
                        postListRange.first + RANGE_INCREMENT,
                        postListRange.last + RANGE_INCREMENT
                    )
                    getTimeline()
                }
            }
        })
    }

    /**
     * adds new items to the recycler view's adapter.
     * @param posts list of posts to add to the adapter
     */
    private fun addTimelineElements(posts: List<GetUserTimelineQuery.GetUserTimeline>) {
        val timelinePosts = ArrayList<PostType>()

        for (post in posts) {
            val postType = PostType(
                post.id().toString(),
                post.userId(),
                post.username(),
                post.userScreenName(),
                post.userAvatar(),
                post.image(),
                post.likes() as List<String>,
                post.comments() as List<String>,
                post.datetime(),
                post.description().toString()
            )

            timelinePosts.add(postType)
        }

        this@HomeFragment.activity?.runOnUiThread {
            val adapter = timeline.adapter as TimelinePostAdapter
            adapter.addItems(timelinePosts)
        }
    }

    /**
     * display a message telling the user that there are posts on their timeline.
     */
    private fun displayMessage() {
        this@HomeFragment.activity!!.runOnUiThread {
            timeline.visibility = View.GONE
            no_posts_text.visibility = View.VISIBLE
        }
    }
}
