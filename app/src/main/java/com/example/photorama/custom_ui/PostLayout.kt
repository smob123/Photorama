package com.example.photorama.custom_ui

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photorama.MainAppActivity
import com.example.photorama.PostViewActivity
import com.example.photorama.R
import com.example.photorama.SearchActivity
import com.example.photorama.adapters.TimelinePostAdapter
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.PostParcelable
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.heplerObjects.TextUtils
import com.example.photorama.networking.Mutations
import com.example.photorama.networking.ServerDomain
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Sultan
 * handles the UI elements in a post.
 * @param mContext activity's context
 * @param post the post's info, which will be shown in the UI
 */

class PostLayout(
    private val mContext: Context,
    private val post: PostType
) : LinearLayout(mContext) {

    init {
        addView(initView())
    }

    /**
     * initializes the post view in order to display it in the layout.
     * @return a post view that will be displayed on this layout
     */
    private fun initView(): View {
        val postView = LayoutInflater.from(mContext).inflate(R.layout.post_layout, this, false)

        val avatarImageView = postView.findViewById<ImageView>(R.id.user_avatar)
        if (post.userAvatarUrl != "null") {
            Glide.with(mContext).load("${ServerDomain().baseUrlString()}${post.userAvatarUrl}")
                .into(avatarImageView)
        } else {
            avatarImageView.setImageResource(R.drawable.avatar)
        }

        // get the postView's image view
        val postImageView = postView.findViewById<ImageView>(R.id.post_image)

        // download the post's image from the given url, and display it
        Glide.with(mContext).load("${ServerDomain().baseUrlString()}${post.image}")
            .into(postImageView)

        // update the username text view
        postView.findViewById<TextView>(R.id.username_txt_view).text = post.userScreenName

        val likeBtn = postView.findViewById<ImageView>(R.id.like_btn)
        val numOfLikes = postView.findViewById<TextView>(R.id.num_of_likes)
        // update the number of likes, and the like icon
        getLikes(likeBtn, numOfLikes, post)

        // update the number of comments
        val numOfComments = post.comments.size

        val numOfCommentsTxt = postView.findViewById<TextView>(R.id.num_of_comments)
        numOfCommentsTxt.text = numOfComments.toString()

        // update the time to show when the post was uploaded
        val datetimeTxt = postView.findViewById<TextView>(R.id.post_date)
        datetimeTxt.text = TextUtils(mContext).getTimeDiff(post.datetime)

        val commentBtn = postView.findViewById<ImageView>(R.id.comment_btn)

        // display the post's description
        val postDescription = postView.findViewById<TextView>(R.id.post_description)
        val spannableComment = TextUtils(mContext).getMentionsAndHashtags(post.description)
        postDescription.text = spannableComment
        postDescription.movementMethod = LinkMovementMethod.getInstance()

        // set onClick listeners
        val usernameContainer = postView.findViewById<ViewGroup>(R.id.username_avatar_container)
        usernameContainer.setOnClickListener {
            setUsernameOnClickListener(post.username)
        }

        val optionsBtn = postView.findViewById<ImageView>(R.id.options_btn)
        optionsBtn.setOnClickListener {
            val popupMenu = setupPopupMenu(optionsBtn, post)
            showPopupMenu(popupMenu, optionsBtn, post)
        }

        val detector = gestureDetector(postView, post)
        postImageView.setOnTouchListener { view, motionEvent ->
            detector.onTouchEvent(motionEvent)
        }

        likeBtn.setOnClickListener {
            likeBtnOnClickListener(likeBtn, numOfLikes, post)
        }

        commentBtn.setOnClickListener {
            commentBtnOnClickListener(post, postImageView)
        }

        return postView
    }

    /**
     * displays to the post's uploader's profile in a new activity.
     * @param username the user's name
     */
    private fun setUsernameOnClickListener(username: String) {
        val intent = Intent(mContext, SearchActivity::class.java)
        intent.putExtra("username", username)

        mContext.startActivity(intent)
    }

    /**
     * returns a GestureDetector object to be added to the post's image
     * @param parent the parent view of the post's image
     * @param postType the post object that contains the post's information
     *
     * @return a GestureDetector object to be added to the post's image
     */
    private fun gestureDetector(
        parent: View,
        postType: PostType
    ): GestureDetector {
        val detector =
            GestureDetector(mContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val heartImage = parent.findViewById<ImageView>(R.id.heart_image)
                    val heartDrawable = heartImage.drawable
                    heartImage.alpha = .70f

                    (heartDrawable as AnimatedVectorDrawable).start()

                    if (!isPostLiked(postType)) {
                        val likeBtn = parent.findViewById<ImageView>(R.id.like_btn)
                        val numOfLikes = parent.findViewById<TextView>(R.id.num_of_likes)
                        likeBtnOnClickListener(likeBtn, numOfLikes, postType)
                    }

                    return true
                }

                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                    return true
                }

                override fun onDown(e: MotionEvent?): Boolean {
                    return true
                }
            })

        return detector
    }

    /**
     * updates the number of likes, and the like image when clicking on the like image.
     * @param likeBtn the button that the listener will be added to
     * @param numOfLikes the textView that displays the number of likes
     * @param postType the post object that contains the post's information
     */
    private fun likeBtnOnClickListener(
        likeBtn: ImageView,
        numOfLikes: TextView,
        postType: PostType
    ) {
        val drawable = likeBtn.drawable.constantState

        val borderHeart =
            mContext.resources.getDrawable(R.drawable.ic_favorite_border_red_300_24dp, null)
        val filledHeart = mContext.resources.getDrawable(R.drawable.ic_favorite_red_300_24dp, null)

        val arrList = ArrayList<String>(postType.likes)
        val json = CacheHandler(mContext).getCache()
        val id = json.get("id").toString()

        if (drawable == borderHeart.constantState) {
            likeBtn.setImageDrawable(filledHeart)
            arrList.add(id)
            likePost(likeBtn, numOfLikes, postType)
        } else {
            likeBtn.setImageDrawable(borderHeart)
            arrList.remove(id)
            unlikePost(likeBtn, numOfLikes, postType)
        }

        postType.likes = arrList
        getLikes(likeBtn, numOfLikes, postType)
    }

    /**
     * goes to the PostViewActivity when the comment image is pressed.
     * @param postType the post object that contains the post's information
     */
    private fun commentBtnOnClickListener(postType: PostType, imageView: ImageView) {
        val activity = this.context as Activity

        if (activity is PostViewActivity) {
            return
        }

        val postParcelable =
            PostParcelable(
                postType.userId,
                postType.username,
                postType.userScreenName,
                postType.id,
                postType.userAvatarUrl,
                postType.likes,
                postType.comments,
                postType.datetime,
                postType.description
            )

        // initialize, and start a post view activity
        val intent = Intent(
            mContext,
            PostViewActivity::class.java
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra("image", postType.image)
        intent.putExtra("postInfo", postParcelable)

        // animation options
        val options = ActivityOptions.makeSceneTransitionAnimation(
            mContext as AppCompatActivity,
            imageView,
            "hero_transition"
        )
        mContext.startActivity(intent, options.toBundle())
    }

    /**
     * calculate the time difference between now, and when the post date was uploaded.
     *
     * @param datetime the post's upload time in the format "EEE MMM d HH:mm:ss z yyyy"
     *
     * @return the time difference between now, and when the post was uploaded, either in minutes,
     * hours, or days
     */
    private fun getTimeDiff(datetime: String): String {
        // setup the time format
        val sdf = SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy")
        // get the post's upload time as a Date object
        val postDate = sdf.parse(datetime)
        // get the time now
        val now = Calendar.getInstance().time
        // calculate the difference in time
        val diff = now.time - postDate.time

        // calculate the difference in minutes
        val minutesDiff = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS)

        // return it if it's less than 60
        if (minutesDiff < 60) {
            return "$minutesDiff minutes ago"
        }

        // calculate the difference in hours
        val hoursDiff = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS)

        // return it if it's less than 24
        if (hoursDiff < 24) {
            return "$hoursDiff hours ago"
        }

        // otherwise, calculate the difference in days and return it
        val daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        return "$daysDiff days ago"
    }

    /**
     * updates the number of likes, and the like icon if needed.
     * @param likeBtn the button that will be used to show whether the user has liked the post
     * or not
     * @param numOfLikesTextView the textView that shows the number of likes for this post
     * @param post the post object that contains details about this post
     */
    private fun getLikes(likeBtn: ImageView, numOfLikesTextView: TextView, post: PostType) {
        // update the number of likes
        val numOfLikes = post.likes.size.toString()

        val activity = mContext as AppCompatActivity
        activity.runOnUiThread {
            numOfLikesTextView.text = numOfLikes
        }
        // check if the user's id is in the post's like list
        if (isPostLiked(post)) {
            likeBtn.setImageResource(R.drawable.ic_favorite_red_300_24dp)
        }
    }

    /**
     * checks if the user has liked a given post or not.
     *
     * @param post the post object that contains the post's information
     *
     * @return whether the user has liked the post or not
     */
    private fun isPostLiked(post: PostType): Boolean {
        // check if the user has liked the post before
        // get the user's id from cache
        val json = CacheHandler(mContext).getCache()
        val id = json.get("id").toString()
        // check if the user's id is in the post's like list
        if ((post.likes).contains(id)) {
            return true
        }

        return false
    }

    /**
     * sends a likePost request to the server.
     * @param likeBtn the button that will be used to send the request
     * @param numOfLikesTextView the textView that shows the number of likes for the post
     * @param post the post object that contains the post's information
     */
    private fun likePost(likeBtn: ImageView, numOfLikesTextView: TextView, post: PostType) {
        Mutations(mContext)
            .likePost(post.id,
                onCompleted = { err, _ ->
                    if (err != null) {
                        likeBtnOnClickListener(likeBtn, numOfLikesTextView, post)
                    }
                })
    }

    /**
     * sends an unlikePost request to the server.
     * @param likeBtn the button that will be used to send the request
     * @param numOfLikesTextView the textView that shows the number of likes for the post
     * @param post the post object that contains the post's information
     */
    private fun unlikePost(likeBtn: ImageView, numOfLikesTextView: TextView, post: PostType) {
        Mutations(mContext)
            .unlikePost(post.id,
                onCompleted = { err, _ ->
                    if (err != null) {
                        likeBtnOnClickListener(likeBtn, numOfLikesTextView, post)
                    }
                })
    }

    /**
     * sets up the on click listener for the popup menu icon.
     * @param optionsBtn the options button that will be used to display the options menu
     * @param post the post that we want to mutate, or find its uploader's information
     */
    private fun setupPopupMenu(optionsBtn: ImageView, post: PostType): PopupMenu {
        // build a popup menu to ask the user to pick an action
        val popupMenu = PopupMenu(mContext, optionsBtn)
        popupMenu.menuInflater.inflate(R.menu.post_view_popup_menu, popupMenu.menu)

        // check if this post belongs to the user
        val json = CacheHandler(mContext).getCache()
        val id = json.get("id").toString()

        if (post.userId != id) {
            val json = CacheHandler(mContext).getCache()
            val following = json.getJSONArray("following")
            val isFollowing = following.toString().contains(post.userId)

            // if the user is following the post's uploader
            if (isFollowing) {
                // hide the delete, and follow buttons
                popupMenu.menu.findItem(R.id.delete_post).isVisible = false
                popupMenu.menu.findItem(R.id.follow_user).isVisible = false
            } else {
                // otherwise hide the delete, and unfollow buttons
                popupMenu.menu.findItem(R.id.delete_post).isVisible = false
                popupMenu.menu.findItem(R.id.unfollow_user).isVisible = false
            }
        } else {
            popupMenu.menu.findItem(R.id.unfollow_user).isVisible = false
            popupMenu.menu.findItem(R.id.follow_user).isVisible = false
        }

        return popupMenu
    }

    /**
     * display the popup menu.
     * @param popupMenu the popup menu to display
     * @param optionsBtn the post's options button
     * @param post the post's info
     */
    private fun showPopupMenu(popupMenu: PopupMenu, optionsBtn: ImageView, post: PostType) {
        // setup the popup menu's on click listener
        popupMenu.setOnMenuItemClickListener { item ->
            // if the user picks delete
            if (item.itemId == R.id.delete_post) {
                // show an alert dialog to ask the user to confirm the action
                val builder = AlertDialog.Builder(mContext)
                builder.setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton(
                        "Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            deletePost(post.id)
                        })
                    .setNegativeButton(
                        "No",
                        DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    .create()
                    .show()
                return@setOnMenuItemClickListener true
            } else if (item.itemId == R.id.unfollow_user) {
                unfollowUser(post, optionsBtn)
                return@setOnMenuItemClickListener true
            } else if (item.itemId == R.id.follow_user) {
                followUser(post, optionsBtn)
            }

            false
        }

        // display the menu
        popupMenu.show()
    }

    /**
     * follow a user.
     * @param post the post that has the user's information
     * @param optionsBtn the button for options menu, that will be updated after the server
     * request is successful
     */
    private fun followUser(post: PostType, optionsBtn: ImageView) {
        Mutations(mContext)
            .follow(post.userId,
                onCompleted = { err, res ->
                    if (res != null) {
                        if (res.follow() == null) {
                            val activity = mContext as AppCompatActivity
                            activity.runOnUiThread() {
                                Toast.makeText(
                                    mContext,
                                    "Error connecting to the network",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@follow
                        }
                        val success = res.follow() as Boolean

                        if (success) {
                            val cacheHandler =
                                CacheHandler(mContext)
                            val jsonObj = cacheHandler.getCache()
                            val following = jsonObj.getJSONArray("following")
                            following.put(post.userId)
                            jsonObj.put("following", following)
                            cacheHandler.overWriteCache(jsonObj)

                            setupPopupMenu(optionsBtn, post)
                        }
                    }
                })
    }

    /**
     * unfollow a user.
     * @param post the post that has the user's information
     * @param optionsBtn the options menu, that will be updated after the server request is successful
     */
    private fun unfollowUser(post: PostType, optionsBtn: ImageView) {
        Mutations(mContext)
            .unfollow(post.userId,
                onCompleted = { err, res ->
                    if (res != null) {
                        val success = res.unfollow() as Boolean
                        if (success) {
                            val cacheHandler =
                                CacheHandler(mContext)
                            val jsonObj = cacheHandler.getCache()
                            val following = jsonObj.getJSONArray("following")
                            for (i in 0 until following.length()) {
                                if (following[i] == post.userId) {
                                    following.remove(i)
                                    break
                                }
                            }
                            jsonObj.put("following", following)
                            cacheHandler.overWriteCache(jsonObj)

                            setupPopupMenu(optionsBtn, post)
                        }
                    }
                })
    }

    /**
     * sends a mutation request to the server to delete the post
     * @param postId the post's id
     */
    private fun deletePost(postId: String) {
        Mutations(mContext).deletePost(postId,
            onCompleted = { err, res ->
                if (res != null) {
                    val activity = this.context as Activity
                    if (activity is MainAppActivity) {
                        activity.runOnUiThread {
                            val recyclerView = parent.parent as RecyclerView
                            val adapter = recyclerView.adapter as TimelinePostAdapter
                            adapter.removeItem(postId)
                        }
                    } else {
                        activity.finish()
                    }
                }
            })
    }
}
