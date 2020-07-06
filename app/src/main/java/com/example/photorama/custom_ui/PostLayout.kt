package com.example.photorama.custom_ui

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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bumptech.glide.Glide
import com.example.photorama.PostViewActivity
import com.example.photorama.R
import com.example.photorama.SearchActivity
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.PostParcelable
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.heplerObjects.TextUtils
import com.example.photorama.networking.ServerDomain
import com.example.photorama.viewModels.PostViewModel
import com.example.photorama.viewModels.PostViewModelFactory
import com.example.photorama.viewModels.UserInteractionsViewModel
import com.example.photorama.viewModels.UserInteractionsViewModelFactory
import org.json.JSONArray

/**
 * @author Sultan
 * handles the UI elements in a post.
 * @param activity the parent activity
 * @param context application's context
 * @param post the post's info, which will be shown in the UI
 */

class PostLayout : LinearLayout {

    // view models
    private lateinit var profileViewModel: UserInteractionsViewModel
    private val viewModel: PostViewModel

    // parent activity
    private val activity: AppCompatActivity

    // post's information
    private val post: PostType

    // the user's id
    private val userId: String

    constructor(activity: AppCompatActivity, context: Context, post: PostType) : super(context) {
        this.activity = activity
        this.post = post

        // initialize the view model
        val factory = PostViewModelFactory(activity)
        viewModel = ViewModelProvider(
            activity as ViewModelStoreOwner,
            factory
        ).get(PostViewModel::class.java)
        viewModel.setPostInfo(post)

        // get the user's id
        val json = CacheHandler(context).getCache()
        userId = json.getString("id")

        // initialize the view
        addView(initView())
    }

    constructor(
        activity: AppCompatActivity,
        context: Context,
        post: PostType,
        viewModel: PostViewModel
    ) : super(context) {
        this.activity = activity
        this.post = post
        this.viewModel = viewModel

        // get the user's id
        val json = CacheHandler(context).getCache()
        userId = json.getString("id")

        // initialize the view
        addView(initView())
    }

    /**
     * initializes the post view in order to display it in the layout.
     * @return a post view that will be displayed on this layout
     */
    private fun initView(): View {
        val postView = LayoutInflater.from(activity).inflate(R.layout.post_layout, this, false)

        val avatarImageView = postView.findViewById<ImageView>(R.id.user_avatar)
        if (post.userAvatarUrl != "null") {
            Glide.with(context).load("${ServerDomain().baseUrlString()}${post.userAvatarUrl}")
                .into(avatarImageView)
        } else {
            avatarImageView.setImageResource(R.drawable.avatar)
        }

        // get the postView's image view
        val postImageView = postView.findViewById<ImageView>(R.id.post_image)

        // download the post's image from the given url, and display it
        Glide.with(context).load("${ServerDomain().baseUrlString()}${post.image}")
            .into(postImageView)

        // update the username text view
        postView.findViewById<TextView>(R.id.username_txt_view).text = post.userScreenName

        val likeBtn = postView.findViewById<ImageView>(R.id.like_btn)
        val numOfLikes = postView.findViewById<TextView>(R.id.num_of_likes)
        // update the number of likes, and the like icon
        numOfLikes.text = post.likes.size.toString()
        // create an observer to monitor changes
        initLikeCountObserver(likeBtn, numOfLikes)

        // update the number of comments
        val numOfComments = post.comments.size

        val numOfCommentsTextView = postView.findViewById<TextView>(R.id.num_of_comments)
        numOfCommentsTextView.text = numOfComments.toString()
        initCommentCountObserver(numOfCommentsTextView)

        // update the time to show when the post was uploaded
        val datetimeTxt = postView.findViewById<TextView>(R.id.post_date)
        datetimeTxt.text = TextUtils(context).getTimeDiff(post.datetime)

        val commentBtn = postView.findViewById<ImageView>(R.id.comment_btn)

        // display the post's description
        val postDescription = postView.findViewById<TextView>(R.id.post_description)
        val spannableComment = TextUtils(context).getMentionsAndHashtags(post.description)
        postDescription.text = spannableComment
        postDescription.movementMethod = LinkMovementMethod.getInstance()

        // set onClick listeners
        val usernameContainer = postView.findViewById<ViewGroup>(R.id.username_avatar_container)
        usernameContainer.setOnClickListener {
            setUsernameOnClickListener(post.username)
        }

        val optionsBtn = postView.findViewById<ImageView>(R.id.options_btn)
        optionsBtn.setOnClickListener {
            val popupMenu = setupPopupMenu(optionsBtn)
            showPopupMenu(popupMenu)
        }
        if (post.userId != userId) {
            val userFactory = UserInteractionsViewModelFactory(activity)
            profileViewModel = ViewModelProvider(activity as ViewModelStoreOwner, userFactory).get(
                UserInteractionsViewModel::class.java
            )
            initFollowUserObserver(optionsBtn, profileViewModel)
            initFollowErrorObserver(profileViewModel)
        }

        val detector = gestureDetector(postView)
        postImageView.setOnTouchListener { view, motionEvent ->
            detector.onTouchEvent(motionEvent)
        }

        likeBtn.setOnClickListener {
            likeBtnOnClickListener(likeBtn, numOfLikes)
        }

        commentBtn.setOnClickListener {
            commentBtnOnClickListener(postImageView)
        }

        return postView
    }

    /**
     * observes changes to whether the user has followed, or unfollowed the post's uploader's account.
     */
    private fun initFollowUserObserver(
        optionsBtn: ImageView,
        profileViewModel: UserInteractionsViewModel
    ) {
        profileViewModel.isInteractionSuccessful()
            .observe(activity as LifecycleOwner, Observer { isSuccessful ->
                if (!isSuccessful) {
                    return@Observer
                }

                // update the cached list of following based on the new user's data
                val cacheHandler =
                    CacheHandler(context)
                val jsonObj = cacheHandler.getCache()
                val following = JSONArray(profileViewModel.getUser().following)
                following.put(post.userId)
                jsonObj.put("following", following)
                cacheHandler.storeLoginCache(jsonObj)

                // update the popup menu
                setupPopupMenu(optionsBtn)
            })
    }

    private fun initFollowErrorObserver(profileViewModel: UserInteractionsViewModel) {
        profileViewModel.getErrorMessage().observe(context as LifecycleOwner, Observer { error ->
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show()
        })
    }

    /**
     * observes changes to the number of comments on the post, and updates the counter accordingly
     */
    private fun initCommentCountObserver(numOfComments: TextView) {
        viewModel.getComments().observe(activity as LifecycleOwner, Observer { comments ->
            numOfComments.text = comments.size.toString()
        })
    }

    /**
     * observes changes to the number of likes on the post, and updates the counter accordingly
     */
    private fun initLikeCountObserver(likeBtn: ImageView, numOfLikes: TextView) {
        viewModel.getLikes().observe(activity as LifecycleOwner, Observer { likes ->
            val isLiked = likes.contains(userId)

            val borderHeart =
                activity.resources.getDrawable(R.drawable.ic_favorite_border_red_300_24dp, null)
            val filledHeart =
                activity.resources.getDrawable(R.drawable.ic_favorite_red_300_24dp, null)

            if (isLiked) {
                likeBtn.setImageDrawable(filledHeart)
            } else {
                likeBtn.setImageDrawable(borderHeart)
            }

            post.likes = likes
            numOfLikes.text = likes.size.toString()
        })
    }

    /**
     * displays to the post's uploader's profile in a new activity.
     * @param username the user's name
     */
    private fun setUsernameOnClickListener(username: String) {
        val intent = Intent(context, SearchActivity::class.java)
        intent.putExtra("username", username)

        activity.startActivity(intent)
    }

    /**
     * returns a GestureDetector object to be added to the post's image
     * @param parent the parent view of the post's image
     * @param postType the post object that contains the post's information
     *
     * @return a GestureDetector object to be added to the post's image
     */
    private fun gestureDetector(
        parent: View
    ): GestureDetector {
        val detector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val heartImage = parent.findViewById<ImageView>(R.id.heart_image)
                    val heartDrawable = heartImage.drawable
                    heartImage.alpha = .70f

                    (heartDrawable as AnimatedVectorDrawable).start()

                    if (!post.likes.contains(userId)) {
                        val likeBtn = parent.findViewById<ImageView>(R.id.like_btn)
                        val numOfLikes = parent.findViewById<TextView>(R.id.num_of_likes)
                        likeBtnOnClickListener(likeBtn, numOfLikes)
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
        numOfLikes: TextView
    ) {
        val likeIcon = likeBtn.drawable.constantState

        val borderHeart =
            activity.resources.getDrawable(R.drawable.ic_favorite_border_red_300_24dp, null)
        if (likeIcon == borderHeart.constantState) {
            viewModel.likePost(post.id)
        } else {
            viewModel.unlikePost(post.id)
        }

        updateLikeButtonIcon(likeBtn, numOfLikes)
    }

    /**
     * updates the like icon, and the number of likes
     */
    private fun updateLikeButtonIcon(
        likeBtn: ImageView,
        numOfLikes: TextView
    ) {
        val likeIcon = likeBtn.drawable.constantState

        val borderHeart =
            activity.resources.getDrawable(R.drawable.ic_favorite_border_red_300_24dp, null)
        val filledHeart = activity.resources.getDrawable(R.drawable.ic_favorite_red_300_24dp, null)

        if (likeIcon == borderHeart.constantState) {
            likeBtn.setImageDrawable(filledHeart)
            numOfLikes.text = (post.likes.size + 1).toString()
        } else {
            likeBtn.setImageDrawable(borderHeart)
            if (post.likes.isNotEmpty()) {
                numOfLikes.text = (post.likes.size - 1).toString()
            }
        }
    }

    /**
     * goes to the PostViewActivity when the comment image is pressed.
     * @param postType the post object that contains the post's information
     */
    private fun commentBtnOnClickListener(imageView: ImageView) {
        if (activity is PostViewActivity) {
            return
        }

        val postParcelable =
            PostParcelable(
                post.userId,
                post.username,
                post.userScreenName,
                post.id,
                post.userAvatarUrl,
                post.likes,
                post.comments,
                post.datetime,
                post.description
            )

        // initialize, and start a post view activity
        val intent = Intent(
            activity,
            PostViewActivity::class.java
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra("image", post.image)
        intent.putExtra("postInfo", postParcelable)

        // animation options
        val options = ActivityOptions.makeSceneTransitionAnimation(
            activity,
            imageView,
            "hero_transition"
        )
        activity.startActivity(intent, options.toBundle())
    }

    /**
     * sets up the on click listener for the popup menu icon.
     * @param optionsBtn the options button that will be used to display the options menu
     * @param post the post that we want to mutate, or find its uploader's information
     */
    private fun setupPopupMenu(optionsBtn: ImageView): PopupMenu {
        // build a popup menu to ask the user to pick an action
        val popupMenu = PopupMenu(context, optionsBtn)
        popupMenu.menuInflater.inflate(R.menu.post_view_popup_menu, popupMenu.menu)

        // check if this post belongs to the user
        if (post.userId != userId) {
            val json = CacheHandler(context).getCache()
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
    private fun showPopupMenu(popupMenu: PopupMenu) {
        // setup the popup menu's on click listener
        popupMenu.setOnMenuItemClickListener { item ->
            // if the user picks delete
            if (item.itemId == R.id.delete_post) {
                // show an alert dialog to ask the user to confirm the action
                val builder = AlertDialog.Builder(activity)
                builder.setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton(
                        "Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            viewModel.deletePost(post.id)
                        })
                    .setNegativeButton(
                        "No",
                        DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    .create()
                    .show()
                return@setOnMenuItemClickListener true
            } else if (item.itemId == R.id.unfollow_user) {
                profileViewModel.unfollowUser(post.userId)
                return@setOnMenuItemClickListener true
            } else if (item.itemId == R.id.follow_user) {
                profileViewModel.followUser(post.userId)
            }

            false
        }

        // display the menu
        popupMenu.show()
    }
}
