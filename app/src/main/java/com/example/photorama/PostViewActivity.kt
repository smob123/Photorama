package com.example.photorama

import com.example.photorama.heplerObjects.CommentType
import com.example.photorama.adapters.CommentsListAdapter
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photorama.custom_ui.PostLayout
import com.example.photorama.heplerObjects.*
import kotlinx.android.synthetic.main.activity_post_view.*
import com.example.photorama.networking.Mutations
import com.example.photorama.networking.Queries

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
    private val RANGE_INCREMENT = 20

    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_view)

        // add a back button to the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Post"

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

            updateUI(postType)
        } else if (postIdExtra != null) {
            postId = postIdExtra
            getPostDetails()
        } else {
            // otherwise go the previous activity, or the main app activity if there isn't a
            // previous activity
            onBackPressed()
        }

        // set the refresh layout's on refresh listener
        setOnRefreshListener()
    }

    /**
     * sets the refresh layout's on refresh listener
     */
    private fun setOnRefreshListener() {
        refresh_layout.setOnRefreshListener {
            // reset the range, and fetch the latest 20 comments
            commentRange = IntRange(0, 20)
            getComments()
        }
    }

    /**
     * initializes the adapter for the recycler view that displays the comments.
     * @param comments a list of comment data to display them in comment views
     */
    private fun initializeCommentsListAdapter(comments: ArrayList<CommentType>) {
        // initialize the adapter, and layout manager
        val adapter = CommentsListAdapter(
            this@PostViewActivity,
            comments
        )
        val manager = LinearLayoutManager(
            this@PostViewActivity.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        post_comments_container.layoutManager = manager
        post_comments_container.adapter = adapter
        // update the number of comments on the UI
        updateNumberOfComments()

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
                        commentRange.first + RANGE_INCREMENT,
                        commentRange.last + RANGE_INCREMENT
                    )

                    getComments()
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
     * fetches the post's information from the server.
     */
    private fun getPostDetails() {
        Queries(this@PostViewActivity.applicationContext)
            .getPostById(postId,
                onCompleted = { err, res ->
                    if (err != null) {
                        // if the post's information couldn't be retrieved, then display an error
                        // message, and go back to the previous activity
                        this@PostViewActivity.runOnUiThread {
                            Toast.makeText(
                                this@PostViewActivity.applicationContext,
                                "Couldn't retrieve post's information from the network",
                                Toast.LENGTH_LONG
                            ).show()
                            onBackPressed()
                        }
                    }

                    if (res != null) {
                        // if the post's information couldn't be retrieved, then display an error
                        // message, and go back to the previous activity
                        if (res.getPostById == null) {
                            this@PostViewActivity.runOnUiThread {
                                Toast.makeText(
                                    this@PostViewActivity.applicationContext,
                                    "Couldn't retrieve post's information from the network",
                                    Toast.LENGTH_LONG
                                ).show()

                                onBackPressed()
                            }
                        }

                        // otherwise get the returned data, and update the UI
                        val post = res.getPostById!!

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

                        this@PostViewActivity.runOnUiThread {
                            updateUI(postType)
                        }
                    }
                })
    }

    /**
     * initializes a post layout object, gets the post's comments and displays them on the activity
     */
    private fun initPostLayout(postType: PostType) {
        val postLayout = PostLayout(
            this@PostViewActivity,
            postType
        )
        post_content.addView(postLayout)
        getComments()

        // remove the directory containing the post image's Uri
        utils.deleteDirectory()
    }

    /**
     * fetches the post's comments from the server.
     */
    private fun getComments() {
        Queries(this@PostViewActivity)
            .getPostComments(postId,
                commentRange.first,
                commentRange.last,
                onCompleted = { err, res ->
                    this@PostViewActivity.runOnUiThread {
                        // check if the refresh layout is refreshing, and set it to false
                        if (refresh_layout.isRefreshing) {
                            refresh_layout.isRefreshing = false
                        }
                    }
                    if (err != null) {
                        // display an error message if comments were not retrieved
                        this@PostViewActivity.runOnUiThread {
                            Toast.makeText(this@PostViewActivity.applicationContext,
                                "Couldn't get comments from the network",
                                Toast.LENGTH_LONG).show()
                        }
                        return@getPostComments
                    }

                    if (res != null) {
                        val commentList = res.getPostComments
                        commentList?.let { list ->
                            this@PostViewActivity.runOnUiThread {
                                // update the number of comments in the UI
                                val numOfComments = list.size
                                val postLayout = post_content[0]
                                val numOfCommentsTxtView =
                                    postLayout.findViewById<TextView>(R.id.num_of_comments)
                                numOfCommentsTxtView.text = numOfComments.toString()
                            }

                            // check if the pos has comments
                            if (list.isNotEmpty()) {
                                // store the comments' data
                                val comments = ArrayList<CommentType>()
                                for (comment in list) {
                                    val commentType =
                                        CommentType(
                                            comment.id().toString(),
                                            comment.comment().toString(),
                                            comment.postId().toString(),
                                            comment.userId().toString(),
                                            comment.username().toString(),
                                            comment.userScreenName().toString(),
                                            comment.userAvatar(),
                                            comment.datetime().toString()
                                        )

                                    comments.add(commentType)
                                }

                                this@PostViewActivity.runOnUiThread {
                                    // initialize the comments list adapter if the adapter hasn't
                                    // been initialized yet, or the comment  range starts from 0
                                    if (post_comments_container.adapter == null || commentRange.first == 0) {
                                        initializeCommentsListAdapter(comments)
                                    } else {
                                        // otherwise add new comments to the list
                                        addCommentsToView(comments)
                                    }
                                }
                            }
                        }
                    }
                })
    }

    /**
     * sets the send button's on click listener, and the comment edit text's action listener
     */
    private fun setSubmitCommentListener() {
        // send button on click listener
        send_comment_btn.setOnClickListener {
            val comment = add_comment_input.text.toString()
            sendComment(comment)
        }

        // edit text's on submit listener
        add_comment_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val comment = add_comment_input.text.toString()
                sendComment(comment)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    /**
     * sends a comment to the server.
     * @param comment the comment we want to send
     */
    private fun sendComment(comment: String) {
        // check that the edit text is not empty
        if (comment.trim() == "") {
            return
        }

        add_comment_input.setText("")

        Mutations(this@PostViewActivity)
            .addComment(postId, comment,
                onCompleted = { err, res ->
                    if (err != null) {
                        this@PostViewActivity.runOnUiThread {
                            Toast.makeText(this@PostViewActivity.applicationContext,
                                "Couldn't send comment to the server",
                                Toast.LENGTH_LONG)
                        }
                    }

                    if (res != null) {
                        val commentInfo = res.addComment

                        commentInfo?.let {
                            // get the comment object from the server
                            val commentType = CommentType(
                                commentInfo.id().toString(),
                                commentInfo.comment().toString(),
                                commentInfo.postId().toString(),
                                commentInfo.userId().toString(),
                                commentInfo.username().toString(),
                                commentInfo.userScreenName().toString(),
                                commentInfo.userAvatar(),
                                commentInfo.datetime().toString()
                            )

                            val list = ArrayList<CommentType>()
                            list.add(commentType)

                            this@PostViewActivity.runOnUiThread {
                                // display it on the UI
                                if (post_comments_container.adapter == null) {
                                    initializeCommentsListAdapter(list)
                                } else {
                                    addCommentsToView(list)
                                }
                            }
                        }
                    }
                })
    }

    /**
     * adds new comments to the comments list adapter.
     * @param comments list of comments to add
     */
    private fun addCommentsToView(comments: ArrayList<CommentType>) {
        // add new comments
        val adapter = post_comments_container.adapter as CommentsListAdapter
        adapter.addItems(comments)
        updateNumberOfComments()
    }

    /**
     * updates the number of comments on the UI.
     */
    private fun updateNumberOfComments() {
        if (post_comments_container.adapter == null) {
            return
        }

        // add new comments
        val adapter = post_comments_container.adapter as CommentsListAdapter
        // update the number of comments
        val numOfComments = adapter.itemCount
        val postLayout = post_content[0]
        val numOfCommentsTxtView = postLayout.findViewById<TextView>(R.id.num_of_comments)
        numOfCommentsTxtView.text = numOfComments.toString()
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
