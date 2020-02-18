package com.example.photorama.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photorama.GetPostsByHashtagQuery
import com.example.photorama.R
import com.example.photorama.adapters.GridViewAdapter
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.Queries
import kotlinx.android.synthetic.main.hashtag_results_fragment.*

/**
 * @author Sultan
 * displays all the posts in a hashtag.
 */

class HashtagPostsFragment : Fragment() {
    private var hashtag = ""

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
        getPosts()
    }

    /**
     * gets posts from the server.
     */
    private fun getPosts() {
        Queries(this@HashtagPostsFragment.activity!!)
            .getPostsByHashtag(hashtag,
                onCompleted = { err, res ->
                    if (err != null) {
                        // display an error message
                        this@HashtagPostsFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@HashtagPostsFragment.activity!!.applicationContext,
                                "Could't get posts from the network",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    if (res != null) {
                        if (res.postsByHashtag == null) {
                            // display an error message
                            this@HashtagPostsFragment.activity?.runOnUiThread {
                                Toast.makeText(
                                    this@HashtagPostsFragment.activity!!.applicationContext,
                                    "Could't get posts from the network",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@getPostsByHashtag
                        }

                        // otherwise update the UI
                        updateUI(res.postsByHashtag!!)
                    }
                })
    }

    /**
     * adds posts to the UI.
     * @param posts the list of posts to add to the adapter.
     */
    private fun updateUI(posts: List<GetPostsByHashtagQuery.GetPostsByHashtag>) {
        // store the result in an array list of PostType
        val postTypes = ArrayList<PostType>()

        for (i in 0 until posts!!.size) {
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


        this@HashtagPostsFragment.activity ?: return

        // initialize the recycler view's adapter, and layout manager
        val adapter = GridViewAdapter(
            this@HashtagPostsFragment.activity!!,
            postTypes
        )

        this@HashtagPostsFragment.activity ?: return

        val manager = GridLayoutManager(
            this@HashtagPostsFragment.activity!!.applicationContext,
            3,
            GridLayoutManager.VERTICAL,
            false
        )

        this@HashtagPostsFragment.activity?.runOnUiThread {
            hashtag_posts.layoutManager = manager
            hashtag_posts.adapter = adapter
        }
    }
}
