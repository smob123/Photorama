package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.PostType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.ExploreRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * view model that fetches posts for the explore screen.
 */

class ExploreViewModel(private val context: Context) : ViewModel() {
    // stores the user's recommended posts
    private var mPosts = MutableLiveData<ArrayList<PostType>>()

    // stores error messages returned from the server
    private val postSuggestionsErrorMessage = MutableLiveData<ErrorMessage>()

    // checks whether a network request is being made or not
    private var isFetching = MutableLiveData<Boolean>()

    // a repository object that is responsible for making network requests
    private val exploreRepo: ExploreRepo

    /**
     * initialize the global variables.
     */
    init {
        exploreRepo = ExploreRepo.getInstance(context)
    }

    /**
     * returns the stored recommended posts.
     */
    fun getRecommendedPosts() = mPosts as LiveData<ArrayList<PostType>>

    fun getPostRecommendationErrorMessage() = postSuggestionsErrorMessage as LiveData<ErrorMessage>

    /**
     * return whether a network request is being made or not.
     */
    fun isFetching() = isFetching as LiveData<Boolean>

    /**
     * fetches, and returns recommended posts.
     */
    fun fetchRecommendedPosts(startIndex: Int, endIndex: Int) {
        isFetching.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = exploreRepo.fetchPosts(startIndex, endIndex)

            if (fetchedData == null) {
                postSuggestionsErrorMessage.postValue(exploreRepo.postSuggestionsErrorMessage)
                isFetching.postValue(false)
                return@launch
            }

            mPosts.postValue(fetchedData)
            isFetching.postValue(false)
        }
    }
}
