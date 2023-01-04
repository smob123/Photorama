package com.example.photorama

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.adapters.CommentsListAdapter
import com.example.photorama.custom_ui.PostLayout
import com.example.photorama.heplerObjects.ImageUriUtils
import com.example.photorama.heplerObjects.PostParcelable
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.viewModels.PostViewModel
import com.example.photorama.viewModels.PostViewModelFactory
import kotlinx.android.synthetic.main.activity_post_view.*

/**
 * @author Sultan
 * handles the post view's data.
 */
class PostViewActivity : AppCompatActivity() {

    // used to get the image's uri
    private lateinit var utils: ImageUriUtils

    // range of comments to get from the server
    private var commentRange = IntRange(0, 20)

    // the amount of increment to increase the range by, in order to fetch more comments
    private val rangeIncrement = 20

    // view model to fetch data from the server
    private lateinit var viewModel: PostViewModel

    // recycler view's adapter
    private lateinit var commentsListAdapter: CommentsListAdapter

    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        // add a back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Post"

        // initialize the view model
        val factory = PostViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory).get(PostViewModel::class.java)

        // try to get either a post parcelable, a post id from intent extras
        val postParcelable = intent.getParcelableExtra<PostParcelable>("postInfo")
        val postIdExtra = intent.getStringExtra("postId")
        utils = ImageUriUtils(this@PostViewActivity.applicationContext)

        // check if one of the extras was passed
        if (postParcelable != null) {
            postId = postParcelable.postId
            val postImage = intent.getStringExtra("image")

            val postType = PostType(
                postParcelable.postId,
                postParcelable.userId,
                postParcelable.username,
                postParcelable.userScreenName,
                postParcelable.userAvatar,
                postImage,
                postParcelable.likes,
                postParcelable.comments,
                postParcelable.datetime,
                postParcelable.description
            )

            viewModel.setPostInfo(postType)
            updateUI(postType)
            viewModel.fetchComments(postId, commentRange.first, commentRange.last)
        } else if (postIdExtra != null) {
            postId = postIdExtra
            viewModel.fetchPostInfo(postId)
            viewModel.fetchComments(postId, commentRange.first, commentRange.last)
        } else {
            // otherwise go the previous activity, or the main app activity if there isn't a
            // previous activity
            onBackPressed()
        }

        // initialize data observers
        initFetchingObserver()
        initPostInfoObserver()
        initCommentsObserver()
        initScrollListener()
        initNetworkErrorObserver()

        // initialize the comments recycler view
        initRecyclerView()

        // set the refresh layout's on refresh listener
        setOnRefreshListener()
    }

    /**
     * initializes and observer to monitor whether data is being fetched from the server or not.
     */
    private fun initFetchingObserver() {
        viewModel.isFetching().observe(this, Observer { isFetching ->
            refresh_layout.isRefreshing = isFetching
        })
    }

    /**
     * initialize an observer to monitor changes to postInfo.
     */
    private fun initPostInfoObserver() {
        viewModel.getPostInfo().observe(this, Observer { postInfo ->
            if (postInfo == null) {
                onBackPressed()
                return@Observer
            }

            updateUI(postInfo)
        })
    }

    /**
     * initialize an observer to monitor changes to the comments list adapter.
     */
    private fun initCommentsObserver() {
        viewModel.getComments().observe(this, Observer { comments ->
            if (comments == null) {
                Toast.makeText(this, "Couldn't fetch comments from the server", Toast.LENGTH_LONG)
                    .show()
                return@Observer
            }

            if (comments.isEmpty() && commentsListAdapter.itemCount == 0) {
                return@Observer
            }

            if (commentRange.first == 0) {
                commentsListAdapter.setItems(comments)
            } else {
                commentsListAdapter.addItems(comments)
            }

            commentsListAdapter.notifyDataSetChanged()
        })
    }

    private fun initNetworkErrorObserver() {
        viewModel.getErrorMessage().observe(this, Observer { error ->
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show()
        })
    }

    /**
     * initialize the recycler view that displays the comments.
     */
    private fun initRecyclerView() {
        commentsListAdapter = CommentsListAdapter(this, ArrayList(), viewModel)
        val manager = LinearLayoutManager(applicationContext)
        post_comments_container.adapter = commentsListAdapter
        post_comments_container.layoutManager = manager
    }

    /**
     * sets the refresh layout's on refresh listener.
     */
    private fun setOnRefreshListener() {
        refresh_layout.setOnRefreshListener {
            // reset the range, and fetch the latest 20 comments
            commentRange = IntRange(0, 20)
            viewModel.fetchComments(postId, commentRange.first, commentRange.last)
        }
    }

    /**
     * initializes the scroll view's scroll listener.
     */
    private fun initScrollListener() {
        // set the on scroll listener to create an infinite scroll view effect
        post_comments_container.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isScrolling = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = true
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val manager = post_comments_container.layoutManager as LinearLayoutManager
                val visibleItems = manager.childCount
                val totalNumOfItems = manager.itemCount
                val scrolledOutItems = manager.findFirstVisibleItemPosition()

                if (isScrolling && (visibleItems + scrolledOutItems) == totalNumOfItems) {
                    isScrolling = false
                    commentRange = IntRange(
                        commentRange.first + rangeIncrement,
                        commentRange.last + rangeIncrement
                    )

                    viewModel.fetchComments(postId, commentRange.first, commentRange.last)
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // set the toolbar's back button click listener
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return false
    }

    /**
     * displays the post, the comments, and sets the comment submit listener.
     * @param postType the post's information
     */
    private fun updateUI(postType: PostType) {
        initPostLayout(postType)
        setSubmitCommentListener()
    }

    /**
     * initializes a post layout object, gets the post's comments and displays them on the activity
     */
    private fun initPostLayout(postType: PostType) {
        val postLayout = PostLayout(
            this,
            this@PostViewActivity,
            postType,
            viewModel
        )
        post_content.removeAllViews()
        post_content.addView(postLayout)
        // remove the directory containing the post image's Uri
        utils.deleteDirectory()
    }

    /**
     * sets the send button's on click listener, and the comment edit text's action listener
     */
    private fun setSubmitCommentListener() {
        // send button on click listener
        send_comment_btn.setOnClickListener {
            submitComment()
        }

        // edit text's on submit listener
        add_comment_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitComment()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    /**
     * sends the text that's in the text input to the server.
     */
    private fun submitComment() {
        // make sure that the text isn't blank
        if (add_comment_input.text.isBlank()) {
            return
        }

        val comment = add_comment_input.text.toString()
        viewModel.sendComment(postId, comment)
        add_comment_input.setText("")
    }

    override fun onBackPressed() {
        // check if this activity is the root activity
        if (isTaskRoot) {
            // go back to the main app's activity
            val intent = Intent(this@PostViewActivity, MainAppActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            // otherwise go back to the previous activity
            finishAfterTransition()
        }
    }
}
