package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.TimelineRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * the home screen's view model, which stores the user's timeline.
 */
class TimelineViewModel(private val context: Context) : ViewModel() {
    // stores the user's timeline
    private val mPosts = MutableLiveData<ArrayList<PostType>>()

    // checks whether a network request is being made or not
    private val isFetching = MutableLiveData<Boolean>()

    // the id of a recently deleted post
    private val deletedPostId = MutableLiveData<String>()

    // a repository object that is responsible for making network requests
    private val timelineRepo: TimelineRepo

    // checks if an operation has failed
    private val errorMessage = MutableLiveData<ErrorMessage>()

    /**
     * initialize the global variables.
     */
    init {
        timelineRepo = TimelineRepo.getInstance(context)
    }

    /**
     * returns the stored timeline posts.
     */
    fun getTimelinePosts() = mPosts as LiveData<ArrayList<PostType>>

    /**
     * returns the id of a recently deleted post.
     */
    fun getDeletedPostId() = deletedPostId as LiveData<String>

    /**
     * returns whether an operation has failed or not
     */
    fun getErrorMessage() = errorMessage as LiveData<ErrorMessage>

    /**
     * return whether a network request is being made or not.
     */
    fun isFetching(): MutableLiveData<Boolean> {
        return isFetching
    }

    /**
     * fetches, and returns timeline posts.
     */
    fun fetchTimelinePosts(startIndex: Int, endIndex: Int, checkCache: Boolean) {
        isFetching.value = true

        // get cached posts first
        if (checkCache) {
            mPosts.value = timelineRepo.getCache()
        }

        // try to fetch newer posts from the network
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = timelineRepo.fetchPosts(startIndex, endIndex)

            if (fetchedData == null) {
                errorMessage.postValue(timelineRepo.errorMessage)
                return@launch
            }

            mPosts.postValue(fetchedData)
            isFetching.postValue(false)
        }
    }

    /**
     * deletes a post from the timeline.
     */
    fun deletePost(postId: String) {
        if (mPosts.value == null) {
            return
        }

        val deleted = timelineRepo.deletePost(postId, mPosts.value!!)
        if (deleted) {
            deletedPostId.value = postId
        }
    }
}
