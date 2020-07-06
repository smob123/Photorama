package com.example.photorama.screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photorama.ImageSelectionActivity
import com.example.photorama.R
import com.example.photorama.SettingsActivity
import com.example.photorama.UserListActivity
import com.example.photorama.adapters.GridViewAdapter
import com.example.photorama.networking.ServerDomain
import com.example.photorama.viewModels.UserInteractionsViewModel
import com.example.photorama.viewModels.UserInteractionsViewModelFactory
import com.example.photorama.viewModels.UserProfileViewModel
import com.example.photorama.viewModels.UserProfileViewModelFactory
import kotlinx.android.synthetic.main.profile_fragment.*

/**
 * @author Sultan
 * handles the profile fragment.
 */
class ProfileFragment : Fragment() {

    // the user's id
    private lateinit var userId: String

    // the user's username
    private lateinit var username: String

    // checks if this profile belongs to the user
    private var mProfile = true

    // the list of people that the user follows
    private var following = ArrayList<String>()

    // the list of of the user's followers
    private var followers = ArrayList<String>()

    // the range of posts to fetch from the server
    private var postRange = IntRange(0, 10)

    // the amount of increment to increase the range by, in order to fetch more posts
    private val rangeIncrement = 10

    // requests the profile's info from the server
    private lateinit var userProfileViewModel: UserProfileViewModel

    // sends follow/unfollow requests if this profile doesn't belong to the user
    private lateinit var userInteractionsViewModel: UserInteractionsViewModel

    // the recycler view's adapter
    private lateinit var postAdapter: GridViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = arguments?.getString("username")

        // initialize the view models
        val factory = UserProfileViewModelFactory(requireActivity())
        userProfileViewModel =
            ViewModelProvider(requireActivity(), factory).get(UserProfileViewModel::class.java)

        val f = UserInteractionsViewModelFactory(requireActivity())
        userInteractionsViewModel =
            ViewModelProvider(requireActivity(), f).get(UserInteractionsViewModel::class.java)

        // initialize the UI elements
        initRecyclerView()
        initUserInfoObserver()
        initLoadingObserver()
        initPostsObserver()
        initPostDeletionObserver()

        // get the user's username
        val mUser = userInteractionsViewModel.getUser()

        // compare it with the passed args if there are any
        mProfile = name == null || name == mUser.username

        // update the ui based on whether this profile belongs to the user, or not
        if (mProfile) {
            username = mUser.username
            setEditProfileBtn()
            setProfileImageOnClickListener()
            userProfileViewModel.fetchUserInfo(
                mUser.username,
                postRange.first,
                postRange.last,
                true
            )
        } else {
            username = name!!
            action_btn.text = getString(R.string.follow_button_title)
            initFollowObserver()
            userProfileViewModel.fetchUserInfo(name, postRange.first, postRange.last, false)
        }

        // set the refresh layout listener
        refreshLayoutListener()
        // set the click listener for the views that show the number of followers, and following
        setFollowersClickListener()
        setFollowingClickListener()
        // set the on scroll listener for the scroll view
        setOnScrollListener()
    }

    /**
     * initialize an observer for network requests, which shows the progress indicator.
     */
    private fun initLoadingObserver() {
        userProfileViewModel.isFetching().observe(requireActivity(), Observer { isFetching ->
            refresh_layout.isRefreshing = isFetching!!
        })
    }

    /**
     * initialize the recycler view that contains the user's posts.
     */
    private fun initRecyclerView() {
        // initialize the recycler view's adapter, and manager
        val manager = GridLayoutManager(
            activity!!,
            3,
            RecyclerView.VERTICAL,
            false
        )

        postAdapter =
            GridViewAdapter(
                requireActivity(),
                ArrayList()
            )

        image_gallery.adapter = postAdapter
        image_gallery.layoutManager = manager
    }

    /**
     * initializes an observer to check for changes to the user's info.
     */
    @SuppressLint("SetTextI18n")
    private fun initUserInfoObserver() {
        userProfileViewModel.getUserInfo().observe(requireActivity(), Observer { info ->
            // show an error message if the request has failed
            if (info == null) {
                Toast.makeText(
                    requireActivity(),
                    "Couldn't connecting to the server",
                    Toast.LENGTH_LONG
                ).show()
                return@Observer
            }

            // set the user's avatar
            if (info.avatar != "null") {
                Glide.with(activity!!)
                    .load("${ServerDomain().baseUrlString()}${info.avatar}")
                    .into(avatar_img)
            } else {
                avatar_img.setImageResource(R.drawable.avatar)
            }

            // set the user's username, screen name, number of posts, etc...
            userId = info.userId
            username_txt_view.text = "@${info.username}"
            screen_name_txt_view.text = info.screenName
            num_of_posts.text = info.posts.size.toString()

            following = ArrayList(info.following)
            followers = ArrayList(info.followers)

            num_of_following.text = following.size.toString()
            num_of_followers.text = followers.size.toString()

            // check if this profile doesn't belong to the user
            val mUser = userInteractionsViewModel.getUser()

            if (mUser.userId != info.userId) {
                // update the follow button to reflect whether the user follows the current
                // account, or not
                updateFollowBtn()
                setFollowBtnClickListener()
            }
        }
        )
    }

    /**
     * initializes an observer to check if the data in the recycler view's adapter has been updated.
     */
    private fun initPostsObserver() {
        userProfileViewModel.getProfilePosts()
            .observe(requireActivity(), Observer { posts ->
                // set/update the adapter's content based on the the starting range
                if (postRange.first == 0) {
                    postAdapter.setItems(posts)
                } else {
                    postAdapter.addItems(posts)
                }

                postAdapter.notifyDataSetChanged()

                // show/hide the recycler view based on whether the adapter is empty or not
                if (postAdapter.itemCount > 0) {
                    image_gallery_container.visibility = View.VISIBLE
                    no_posts_text.visibility = View.GONE
                } else {
                    image_gallery_container.visibility = View.GONE
                    no_posts_text.visibility = View.VISIBLE
                }
            }
            )
    }

    /**
     * removes posts that were deleted by the user from the timeline.
     */
    private fun initPostDeletionObserver() {
        userProfileViewModel.getDeletedPostId().observe(requireActivity(), Observer { postId ->
            for (i in 0 until postAdapter.itemCount) {
                val post = postAdapter.getItems()[i]
                if (post.id == postId) {
                    postAdapter.removeItem(i)
                    break
                }
            }

            postAdapter.notifyDataSetChanged()
        })
    }

    /**
     * initializes an observer for follow/unfollow requests.
     */
    private fun initFollowObserver() {
        userInteractionsViewModel.isInteractionSuccessful()
            .observe(requireActivity(), Observer { isSuccessful ->
                // check if the request was unsuccessful
                if (!isSuccessful) {
                    // reset the follow button's text, and click listener
                    updateFollowBtn()
                    setFollowBtnClickListener()
                    return@Observer
                }

                // update the list of followers
                val mUser = userInteractionsViewModel.getUser()
                if (followers.contains(mUser.userId)) {
                    followers.remove(mUser.userId)
                } else {
                    followers.add(mUser.userId)
                }

                // update the text view
                num_of_followers.text = followers.size.toString()
            })
    }

    /**
     * sets the on refresh listener for the refresh layout.
     */
    private fun refreshLayoutListener() {
        refresh_layout.setOnRefreshListener {
            // reset the range, and fetch the last 10 posts from the server
            postRange = IntRange(0, 10)
            userProfileViewModel.fetchUserInfo(username, postRange.first, postRange.last, false)
        }
    }

    /**
     * set the click listener for the view that shows the number of accounts the user is following.
     */
    private fun setFollowingClickListener() {
        num_of_following_container.setOnClickListener {
            // go to user list activity to show the list of people this user is following
            val intent = Intent(
                this@ProfileFragment.activity!!.applicationContext,
                UserListActivity::class.java
            )
            intent.putStringArrayListExtra("userIds", following)
            this@ProfileFragment.activity?.startActivity(intent)
        }
    }

    /**
     * set the click listener for the view that shows the number of accounts that follow the user.
     */
    private fun setFollowersClickListener() {
        num_of_followers_container.setOnClickListener {
            // go to user list activity to show this user's followers
            val intent = Intent(
                activity!!.applicationContext,
                UserListActivity::class.java
            )
            intent.putStringArrayListExtra("userIds", followers)
            activity!!.startActivity(intent)
        }
    }

    /**
     * changes the action button's text to "settings", and sets its on click listener.
     */
    private fun setEditProfileBtn() {
        action_btn.text = getString(R.string.settings_button_title)

        action_btn.setOnClickListener {
            // go to the settings activity
            val intent = Intent(
                this@ProfileFragment.activity!!,
                SettingsActivity::class.java
            )

            this@ProfileFragment.activity?.startActivity(intent)
        }
    }

    /**
     * sets the on click listener for the follow button.
     */
    private fun setFollowBtnClickListener() {
        // check if the user follows this account
        val mUser = userInteractionsViewModel.getUser()
        val isFollowed = followers.contains(mUser.userId)
        action_btn.setOnClickListener {
            if (!isFollowed) {
                // if the user isn't following the account, then send a follow request
                userInteractionsViewModel.followUser(userId)
                action_btn.text = getString(R.string.following_button_title)
            } else {
                // otherwise send an unfollow request
                userInteractionsViewModel.unfollowUser(userId)
                action_btn.text = getString(R.string.follow_button_title)
            }
        }
    }

    /**
     * updates the follow button to either "follow", or "following" based on whether the
     * user follows the account or not.
     */
    private fun updateFollowBtn() {
        // check if the user follows this account
        val mUser = userInteractionsViewModel.getUser()
        if (followers.contains(mUser.userId)) {
            action_btn.text = getString(R.string.following_button_title)
        } else {
            action_btn.text = getString(R.string.follow_button_title)
        }
    }

    /**
     * initializes the profile image's on click listener.
     */
    private fun setProfileImageOnClickListener() {
        avatar_img.setOnClickListener {
            displayAlertDialog()
        }
    }

    /**
     * displays an option dialog asking the user what they want to do with their profile image.
     */
    private fun displayAlertDialog() {
        val options = arrayOf("Change Image", "Remove Image")
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Choose an action")
        dialogBuilder.setItems(options) { _, index ->
            when (index) {
                0 -> {
                    changeImage()
                }
                1 -> {
                    removeImage()
                }
            }
        }.show()
    }

    /**
     * goes to the image selection activity, so the user can select a new profile image.
     */
    private fun changeImage() {
        val intent = Intent(
            this@ProfileFragment.activity!!.applicationContext,
            ImageSelectionActivity::class.java
        )
        intent.putExtra("image type", ImageSelectionActivity.ImageType.AVATAR)
        startActivity(intent)
    }

    /**
     * restores the avatar image to default.
     */
    private fun removeImage() {
        userProfileViewModel.removeAvatar()
        avatar_img.setImageResource(R.drawable.avatar)
    }

    /**
     * sets the scroll view's on scroll listener.
     */
    private fun setOnScrollListener() {
        scroll_view.viewTreeObserver.addOnScrollChangedListener(object :
            ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                if (scroll_view == null) {
                    return
                }

                val childCount = scroll_view.childCount
                val lastChild = scroll_view.getChildAt(childCount - 1)

                val diff = lastChild.bottom - (scroll_view.height + scroll_view.scrollY)

                // check if the user has scrolled to the bottom of the scroll view
                if (diff == 0) {
                    // increase the range of posts, and request the next set of posts
                    postRange = IntRange(
                        postRange.first + rangeIncrement,
                        postRange.last + rangeIncrement
                    )

                    userProfileViewModel.fetchProfilePosts(postRange.first, postRange.last)
                }
            }
        })
    }
}
