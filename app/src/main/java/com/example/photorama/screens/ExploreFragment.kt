package com.example.photorama.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.GetPostRecommendationsQuery
import com.example.photorama.R
import com.example.photorama.SearchActivity
import com.example.photorama.adapters.GridViewAdapter
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.Queries
import kotlinx.android.synthetic.main.explore_fragment.*

/**
 * @author Sultan
 * explore posts fragment.
 */

class ExploreFragment : Fragment() {
    // range of posts to fetch from the network
    private var postRange = IntRange(0, 20)
    // the amount of increment to increase the range by, in order to fetch more comments
    private val RANGE_INCREAMENT = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRefreshLayoutListener()
        setSearchBarClickListener()
        addOnScrollListener()
        getRecommendedPosts()
    }

    /**
     * set the refresh layout's on refresh listener.
     */
    private fun setRefreshLayoutListener() {
        refresh_layout.setOnRefreshListener {
            // reset the post range to 20 new posts
            postRange = IntRange(0, 20)
            getRecommendedPosts()
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
     * get post recommendations from the server.
     */
    private fun getRecommendedPosts() {
        Queries(this@ExploreFragment.activity!!)
            .getPostRecommendations(
                postRange.first, postRange.last,
                onCompleted = { err, res ->

                    // stop the refresh layout from refreshing
                    this@ExploreFragment.activity?.let {
                        if (refresh_layout.isRefreshing) {
                            this@ExploreFragment.activity?.runOnUiThread {
                                refresh_layout.isRefreshing = false
                            }
                        }
                    }

                    if (err != null) {
                        // if the posts couldn't be retrieved, then display an error message
                        this@ExploreFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@ExploreFragment.activity?.applicationContext,
                                "Couldn't retrieve posts from the network",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@getPostRecommendations
                    }

                    if (res != null) {
                        val posts = res.postRecommendations
                        val adapter = explore_posts.adapter

                        if (posts == null) {
                            // if the posts couldn't be retrieved, then display an error message
                            this@ExploreFragment.activity?.runOnUiThread {
                                Toast.makeText(
                                    this@ExploreFragment.activity?.applicationContext,
                                    "Couldn't retrieve posts from the network",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@getPostRecommendations
                        }

                        // if the server has returned a non-empty list, or the range starts with 0
                        if ((posts.isNotEmpty() && adapter == null) || postRange.first == 0) {
                            // initialize a new adapter for the recycler view
                            displayPosts(posts)
                            // otherwise if the server has returned a non-empty list,
                            // and the adapter was already initialized
                        } else if (posts.isNotEmpty() && adapter != null) {
                            // add more posts to the adapter
                            addMorePosts(posts)
                            // otherwise if the server has returned less posts than the range, or
                            // nothing at all
                        } else if (posts.isEmpty() || posts.size < (postRange.last - postRange.first)) {
                            // remove the on scroll listener from the recycler view,as this means
                            // that the server has no more posts to send
                            this@ExploreFragment.activity?.runOnUiThread {
                                explore_posts.clearOnScrollListeners()
                            }
                        }
                    }
                })
    }

    /**
     * initializes a new adapter for the recycler view.
     * @param posts the list of posts to add to the adapter
     */
    private fun displayPosts(posts: List<GetPostRecommendationsQuery.GetPostRecommendation>) {
        // store the posts as a list of postType
        val postTypes = ArrayList<PostType>()

        for (i in 0 until posts.size) {
            val postType = PostType(
                posts[i].id().toString(),
                posts[i].userId(),
                posts[i].username(),
                posts[i].userScreenName(),
                posts[i].userAvatar(),
                posts[i].image(),
                posts[i].likes() as List<String>,
                posts[i].comments() as List<String>,
                posts[i].datetime(),
                posts[i].description().toString()
            )

            postTypes.add(postType)
        }

        if (this@ExploreFragment.activity != null) {
            // initialize the adapter
            val adapter = GridViewAdapter(
                this@ExploreFragment.activity!!,
                postTypes
            )

            // initialize the manager
            val manager = GridLayoutManager(
                this@ExploreFragment.activity!!,
                3,
                RecyclerView.VERTICAL,
                false
            )

            // add them to the recycler view
            this@ExploreFragment.activity?.runOnUiThread {
                explore_posts.layoutManager = manager
                explore_posts.adapter = adapter
            }
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
                        postRange.first + RANGE_INCREAMENT,
                        postRange.last + RANGE_INCREAMENT
                    )

                    getRecommendedPosts()
                }
            }
        })
    }

    /**
     * adds more posts to the recycler view's adapter.
     * @param newPots the list of new posts to add
     */
    private fun addMorePosts(newPots: List<GetPostRecommendationsQuery.GetPostRecommendation>) {
        // store the posts as a list of postType
        val posts = ArrayList<PostType>()

        for (post in newPots) {
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

            posts.add(postType)
        }

        // add them to the adapter
        this@ExploreFragment.activity?.runOnUiThread {
            val adapter = explore_posts.adapter as GridViewAdapter
            adapter.addItems(posts)
        }
    }
}
