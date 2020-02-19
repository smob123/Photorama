package com.example.photorama.screens

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.photorama.*
import com.example.photorama.adapters.GridViewAdapter
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.Mutations
import com.example.photorama.networking.Queries
import com.example.photorama.networking.ServerDomain
import kotlinx.android.synthetic.main.profile_fragment.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Sultan
 * handles the profile fragment.
 */
class ProfileFragment : Fragment() {

    // the user's username
    private lateinit var username: String
    // the user's id
    private lateinit var userId: String
    // the user's screen name
    private var screenName: String = "Screen Name"
    // the url to the user's avatar
    private var userAvatarUrl: String = "null"
    // the list of people that the user follows
    private var following = ArrayList<String>()
    // the list of of the user's followers
    private var followers = ArrayList<String>()
    // the range of posts to fetch from the server
    private var postRange = IntRange(0, 10)
    // the amount of increment to increase the range by, in order to fetch more posts
    private val RANGE_INCREMENT = 10

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

        // get the user's id, and username from the cache file
        val jsonObj = CacheHandler(this@ProfileFragment.activity!!.applicationContext).getCache()
        val mId = jsonObj.get("id").toString()
        val mName = jsonObj.get("username").toString()
        val mScreenName = jsonObj.get("screenName").toString()
        val myFollowers = jsonObj.getJSONArray("followers")
        val myFollowing = jsonObj.getJSONArray("following")

        // check if a username wasn't passed as args, or if the username that's passed
        // belongs to another account
        if (name != null && name != mName) {
            username = "@$name"
        } else {
            // initialize the variables to match what's in the cache
            userId = mId
            username = "@$mName"
            screenName = mScreenName

            for (i in 0 until myFollowers.length()) {
                followers.add(myFollowers[i].toString())
            }

            for (i in 0 until myFollowing.length()) {
                following.add(myFollowing[i].toString())
            }

            // update the text views to display the user's cached info
            username_txt_view.text = username
            screen_name_txt_view.text = screenName
            num_of_followers.text = followers.size.toString()
            num_of_following.text = following.size.toString()

            // set the profile image's onClick listener
            setProfileImageOnClickListener()
            setEditProfileBtn()
        }

        // set the refresh layout listener
        refreshLayoutListener()

        // set the click listener for the views that show the number of followers, and following
        setFollowersClickListener()
        setFollowingClickListener()

        // set the on scroll listener for the scroll view
        setOnScrollListener()

        // get user's profile info from the server
        getUserInfo()
        getUserPosts()
    }

    /**
     * sets the on refresh listener for the refresh layout.
     */
    private fun refreshLayoutListener() {
        refresh_layout.setOnRefreshListener {
            postRange = IntRange(0, 10)
            getUserInfo()
            getUserPosts()
        }
    }

    /**
     * set the click listener for the view that shows the number of accounts the user is following.
     */
    private fun setFollowingClickListener() {
        num_of_following_container.setOnClickListener {
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
            val intent = Intent(
                this@ProfileFragment.activity!!.applicationContext,
                UserListActivity::class.java
            )
            intent.putStringArrayListExtra("userIds", followers)
            this@ProfileFragment.activity?.startActivity(intent)
        }
    }

    /**
     * changes the action button's text to "settings", and sets its on click listener.
     */
    private fun setEditProfileBtn() {
        action_btn.text = "Settings"

        action_btn.setOnClickListener {
            val intent = Intent(
                this@ProfileFragment.activity!!,
                SettingsActivity::class.java
            )

            this@ProfileFragment.activity?.startActivity(intent)
        }
    }

    /**
     * checks if the user is following this account, and changes the action button's text to
     * "following" if this account's id is in the user's cache.
     */
    private fun setFollowBtn() {
        // check if this account's id is in the user's cache
        val cacheHandler = CacheHandler(activity!!.applicationContext)
        val jsonObj = cacheHandler.getCache()
        val following = jsonObj.getJSONArray("following")
        val isFollowing = following.toString().contains(userId)

        // if it does, then update the text on the action button to "following"
        if (isFollowing) {
            updateFollowBtn()
        }
    }

    /**
     * sets the on click listener for the follow button.
     */
    private fun setFollowBtnClickListener() {
        action_btn.setOnClickListener {
            // update the button's text
            updateFollowBtn()
            // check if this account's id is in the user's cache
            val cacheHandler = CacheHandler(activity!!.applicationContext)
            val jsonObj = cacheHandler.getCache()
            val following = jsonObj.getJSONArray("following")
            val isFollowing = following.toString().contains(userId)

            // if it does, send a "follow" mutation to the server
            if (!isFollowing) {
                followAccount()
            } else {
                // otherwise, send a "unfollow" mutation to the server
                unfollowAccount()
            }
        }
    }

    /**
     * sends a "follow" mutation to the server.
     */
    private fun followAccount() {
        Mutations(this@ProfileFragment.activity!!)
            .follow(userId,
                onCompleted = { err, res ->
                    if (err != null) {
                        // if an error occurs, then reset the follow button's text
                        this@ProfileFragment.activity?.runOnUiThread {
                            updateFollowBtn()
                        }
                        return@follow
                    }

                    if (res != null) {
                        if (res.follow() == null) {
                            // if an error occurs, then reset the follow button's text
                            this@ProfileFragment.activity?.runOnUiThread {
                                updateFollowBtn()
                            }
                            return@follow
                        }

                        val success = res.follow() as Boolean
                        // if the request is successful
                        if (success) {
                            // get the list of accounts the user is following from the cache
                            val cacheHandler = CacheHandler(activity!!.applicationContext)
                            val jsonObj = cacheHandler.getCache()
                            val following = jsonObj.getJSONArray("following")

                            // add this account's id to it, and save it
                            following.put(userId)
                            jsonObj.put("following", following)
                            cacheHandler.overWriteCache(jsonObj)

                            // update the number of this account's followers
                            this@ProfileFragment.activity!!.runOnUiThread {
                                val numOfFollowers =
                                    num_of_followers?.text.toString().toInt()
                                num_of_followers?.text = (numOfFollowers + 1).toString()
                            }
                        } else {
                            // if an error occurs, then reset the follow button's text
                            this@ProfileFragment.activity?.runOnUiThread {
                                updateFollowBtn()
                            }
                        }
                    }
                })
    }

    /**
     * sends a "unfollow" mutation to the server
     */
    private fun unfollowAccount() {
        Mutations(this@ProfileFragment.activity!!)
            .unfollow(userId,
                onCompleted = { err, res ->
                    if (err != null) {
                        // if an error occurs, then reset the follow button's text
                        this@ProfileFragment.activity?.runOnUiThread {
                            updateFollowBtn()
                        }
                        return@unfollow
                    }

                    if (res != null) {
                        if (res.unfollow() == null) {
                            // if an error occurs, then reset the follow button's text
                            this@ProfileFragment.activity?.runOnUiThread {
                                updateFollowBtn()
                            }
                            return@unfollow
                        }

                        val success = res.unfollow() as Boolean
                        if (success) {
                            // get the list of accounts the user is following from the cache
                            val cacheHandler = CacheHandler(activity!!.applicationContext)
                            val jsonObj = cacheHandler.getCache()
                            val following = jsonObj.getJSONArray("following")

                            // remove this user's id from it
                            for (i in 0 until following.length()) {
                                if (following[i] == userId) {
                                    following.remove(i)
                                    break
                                }
                            }
                            // update the cache
                            jsonObj.put("following", following)
                            cacheHandler.overWriteCache(jsonObj)

                            // upate the number of this account's followers
                            this@ProfileFragment.activity!!.runOnUiThread {
                                val numOfFollowers =
                                    num_of_followers.text.toString().toInt()
                                num_of_followers.text = (numOfFollowers - 1).toString()
                            }
                        }
                    }
                })
    }

    /**
     * updates the follow button to either "follow", or "following" based on whether the
     * user follows the account or not.
     */
    private fun updateFollowBtn() {
        val btnText = action_btn.text.toString().toUpperCase(Locale.getDefault())

        if (btnText == "FOLLOW") {
            action_btn.text = "Following"
        } else {
            action_btn.text = "Follow"
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
        dialogBuilder.setItems(options, DialogInterface.OnClickListener { dialog, index ->
            when (index) {
                0 -> {
                    changeImage()
                }
                1 -> {
                    removeImage()
                }
            }
        }).show()
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
        // upload a null base64 string to remove the image from the server
        Mutations(activity!!.applicationContext)
            .deleteAvatar(onCompleted = { err, res ->
                if (err != null) {
                    return@deleteAvatar
                }
                if (res != null) {
                    getUserInfo()
                }
            })
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

                if (diff == 0) {
                    postRange = IntRange(
                        postRange.first + RANGE_INCREMENT,
                        postRange.last + RANGE_INCREMENT
                    )
                    getUserPosts()
                }
            }
        })
    }

    /**
     * gets user's screen name, number of followers, and following from the server, and updates the
     * UI accordingly.
     */
    private fun getUserInfo() {
        val queryParam = username.substring(1, username.length)
        Queries(this@ProfileFragment.activity!!.applicationContext)
            .getUserByName(queryParam,
                onCompleted = { err, res ->
                    if (err != null) {
                        this@ProfileFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@ProfileFragment.activity?.applicationContext,
                                "Couldn't retrieve user's info from the network",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@getUserByName
                    }

                    if (res != null) {
                        if (res.userByName == null) {
                            this@ProfileFragment.activity?.runOnUiThread {
                                Toast.makeText(
                                    this@ProfileFragment.activity?.applicationContext,
                                    "Couldn't retrieve user's info from the network",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@getUserByName
                        }

                        // get the user's info
                        val userInfo = res.userByName!!

                        // update the list of followers, and following
                        followers = ArrayList(userInfo.followers()!!)
                        following = ArrayList(userInfo.following()!!)

                        // get the number of followers, and following
                        val numOfFollowers = followers.size
                        val numOfFollowing = following.size

                        // update the UI
                        this@ProfileFragment.activity?.runOnUiThread {
                            // get the number of posts, followers, and following
                            val numOfPosts = userInfo.posts()!!.size
                            // update the number of posts
                            num_of_posts.text = numOfPosts.toString()
                            // get the user's screen name, and id
                            if (screenName == "Screen Name") {
                                userId = userInfo.id().toString()
                                // get the user's screen name
                                screenName = userInfo.screenName().toString()

                                // update the username, and screen name text views
                                username_txt_view.text = username
                                screen_name_txt_view.text = screenName

                                // set the follow button's text, and click listener
                                setFollowBtn()
                                setFollowBtnClickListener()
                            } else {
                                // otherwise just update the number of followers, and following
                                // in the cache
                                val jsonObj =
                                    CacheHandler(this@ProfileFragment.activity!!.applicationContext).getCache()

                                jsonObj.put("followers", JSONArray(followers))
                                jsonObj.put("following", JSONArray(following))
                            }

                            // update the number of followers, and following in the UI
                            num_of_followers.text = numOfFollowers.toString()
                            num_of_following.text = numOfFollowing.toString()

                            // get the profile image from the network
                            userAvatarUrl = userInfo.avatar().toString()
                            if (userAvatarUrl != "null") {
                                Glide.with(this@ProfileFragment.activity!!)
                                    .load("${ServerDomain().baseUrlString()}$userAvatarUrl")
                                    .into(avatar_img)
                            } else {
                                avatar_img.setImageResource(R.drawable.avatar)
                            }
                        }
                    }
                })
    }

    /**
     * requests user's posts from the server.
     */
    private fun getUserPosts() {
        val queryParam = username.substring(1, username.length)

        // make the query request
        Queries(activity!!).getUserPosts(
            queryParam,
            postRange.first,
            postRange.last,
            onCompleted = { err, result ->
                this@ProfileFragment.activity?.let {
                    // stop the refresh layout from refreshing
                    if (refresh_layout.isRefreshing) {
                        this@ProfileFragment.activity!!.runOnUiThread {
                            refresh_layout.isRefreshing = false
                        }
                    }
                }

                if (err != null) {
                    // display an error message if there is an error
                    this@ProfileFragment.activity?.runOnUiThread {
                        Toast.makeText(
                            this@ProfileFragment.activity!!.applicationContext,
                            "A network occurred while connecting to the network",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    return@getUserPosts
                }

                if (result != null) {
                    if (result.userPosts == null) {
                        // display an error message if there is an error
                        this@ProfileFragment.activity?.runOnUiThread {
                            Toast.makeText(
                                this@ProfileFragment.activity!!.applicationContext,
                                "A network occurred while connecting to the network",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        return@getUserPosts
                    }

                    // get the user's posts' info
                    val posts = result.userPosts!!

                    // check if the result is not empty
                    if (posts.isNotEmpty()) {
                        // if the adapter hasn't been initialized, or the range starts at 0
                        if (image_gallery.adapter == null || postRange.first == 0) {
                            // add a new adapter to the recycler view, and display the posts
                            displayPosts(posts)
                        } else {
                            // otherwise, add more posts
                            addMorePosts(posts)
                        }
                    } else {
                        // otherwise, if the adapter hasn't been initialized, or the adapter has not
                        // items
                        if (image_gallery?.adapter == null || image_gallery?.adapter!!.itemCount == 0) {
                            // display the text view
                            displayMessage()
                        }
                    }
                }
            }
        )
    }

    /**
     * display a message telling the user that there are no posts for this profile.
     */
    private fun displayMessage() {
        this@ProfileFragment.activity?.runOnUiThread {
            // hide the recycler view from the layout, and replace it with a text view instead
            image_gallery_container.visibility = View.GONE
            no_posts_text.visibility = View.VISIBLE
        }
    }

    /**
     * displays the posts that are returned from the server.
     * @param posts the list of the user's posts that are returned from the server
     */
    private fun displayPosts(posts: List<GetUserPostsQuery.GetUserPost>) {
        // add the post's data to an array list of PostType
        val postTypes = ArrayList<PostType>()

        for (i in 0 until posts.size) {
            val post = posts[i]
            val postType = PostType(
                post.id().toString(),
                post.userId(),
                post.username(),
                screenName,
                userAvatarUrl,
                post.image(),
                post.likes() as List<String>,
                post.comments() as List<String>,
                post.datetime(),
                post.description().toString()
            )

            postTypes.add(postType)
        }

        this@ProfileFragment.activity?.let { activity ->
            // initialize the adapter, and layout manager
            val postAdapter = GridViewAdapter(
                activity,
                postTypes
            )

            val manager = GridLayoutManager(
                activity,
                3,
                RecyclerView.VERTICAL,
                false
            )

            activity.runOnUiThread {
                // hide the text view from the layout, and replace it with a recycler view instead
                image_gallery_container.visibility = View.VISIBLE
                no_posts_text.visibility = View.GONE

                image_gallery.layoutManager = manager
                // set the post adapter, and attach it to the grid view
                image_gallery.adapter = postAdapter
            }
        }
    }

    /**
     * adds more posts to the recycler view.
     * @param posts the list of posts to add to the view
     */
    private fun addMorePosts(posts: List<GetUserPostsQuery.GetUserPost>) {
        // verify that the adapter has been initialized, and that the user hasn't gone to another
        // activity
        if (image_gallery == null || image_gallery.adapter == null) {
            return
        }

        // get the adapter
        val adapter = image_gallery.adapter as GridViewAdapter

        // add the new posts
        val postTypes = ArrayList<PostType>()

        for (i in 0 until posts.size) {
            val post = posts[i]
            //val image = ClearImageCache(post.image()).execute().get()
            val postType = PostType(
                post.id().toString(),
                post.userId(),
                post.username(),
                screenName,
                userAvatarUrl,
                post.image(),
                post.likes() as List<String>,
                post.comments() as List<String>,
                post.datetime(),
                post.description().toString()
            )

            postTypes.add(postType)
        }

        // add new items to the adapter
        this@ProfileFragment.activity?.runOnUiThread {
            adapter.addItems(postTypes)
        }
    }
}
