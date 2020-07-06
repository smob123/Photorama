package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.UserProfileRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * view model that fetches data for the profile screen.
 */

class UserProfileViewModel(private val context: Context) : ViewModel() {
    // stores the user's profile information; such as, username, screen name, etc...
    private val mUser = MutableLiveData<UserType>()

    // stores the user's posts
    private val mPosts = MutableLiveData<ArrayList<PostType>>()

    // the id of a recently deleted post
    private val deletedPostId = MutableLiveData<String>()

    // checks whether a network request is being made or not
    private val isFetching = MutableLiveData<Boolean>()

    private val avatarUploaded = MutableLiveData<Boolean>()

    private val errorMessage = MutableLiveData<ErrorMessage>()

    // a repository object that is responsible for making network requests
    private val userProfileRepo: UserProfileRepo

    init {
        userProfileRepo = UserProfileRepo.getInstance(context)
    }

    /**
     * returns a user's stored info.
     */
    fun getUserInfo() = mUser

    /**
     * returns a user's stored posts.
     */
    fun getProfilePosts() = mPosts

    /**
     * returns the id of a recently deleted post.
     */
    fun getDeletedPostId() = deletedPostId as LiveData<String>

    /**
     * returns true if a new avatar was successfully sent to the server.
     */
    fun isAvatarUploaded() = avatarUploaded as LiveData<Boolean>

    /**
     * returns error messages that were returned from the server.
     */
    fun getErrorMessage() = errorMessage as LiveData<ErrorMessage>

    /**
     * return whether a network request is being made or not.
     */
    fun isFetching() = isFetching

    /**
     * fetches, and returns user's profile info.
     */
    fun fetchUserInfo(username: String, startIndex: Int, endIndex: Int, checkCache: Boolean) {
        // get the user's info from the cache to display it first
        if (checkCache) {
            mUser.value = userProfileRepo.getCachedInfo()
            mPosts.value = userProfileRepo.getPostsCache()
        }

        // then get the latest info from the server
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = userProfileRepo.fetchUserInfo(username)

            if (fetchedData == null) {
                errorMessage.postValue(userProfileRepo.errorMessage)
                return@launch
            }

            mUser.postValue(fetchedData)
            fetchProfilePosts(startIndex, endIndex)
        }
    }

    /**
     * fetches, and returns user's profile posts.
     */
    fun fetchProfilePosts(startIndex: Int, endIndex: Int) {
        isFetching.postValue(true)

        // get the data from the server
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = userProfileRepo.fetchPosts(mUser.value!!, startIndex, endIndex)

            if (fetchedData == null) {
                errorMessage.postValue(userProfileRepo.errorMessage)
                return@launch
            }

            mPosts.postValue(fetchedData)
            isFetching.postValue(false)
        }
    }

    fun uploadAvatar(base64Image: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val uploaded = userProfileRepo.uploadAvatar(base64Image)

            if (!uploaded) {
                errorMessage.postValue(userProfileRepo.errorMessage)
                return@launch
            }

            avatarUploaded.postValue(uploaded)
        }
    }

    /**
     * sends a request to remove the user's avatar.
     */
    fun removeAvatar() {
        CoroutineScope(Dispatchers.IO).launch {
            val removed = userProfileRepo.removeAvatar()

            // check if the avatar wasn't removed
            if (!removed) {
                errorMessage.postValue(userProfileRepo.errorMessage)
            } else if (removed) {
                val user = mUser.value
                user!!.avatar = "null"
                mUser.postValue(user)
            }
        }
    }

    fun deletePost(postId: String) {
        if (mPosts.value == null) {
            return
        }

        val deleted = userProfileRepo.deletePost(postId, mPosts.value!!)
        if (deleted) {
            deletedPostId.value = postId
        }
    }
}
