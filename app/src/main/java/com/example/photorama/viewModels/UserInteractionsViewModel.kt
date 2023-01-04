package com.example.photorama.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.photorama.heplerObjects.CacheHandler
import com.example.photorama.heplerObjects.UserType
import com.example.photorama.networking.ErrorMessage
import com.example.photorama.repositories.UserInteractionsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

class UserInteractionsViewModel(private val context: Context) : ViewModel() {
    // checks whether a network request is being made or not.
    private val isFetching = MutableLiveData<Boolean>()

    private val interactionSuccessful = MutableLiveData<Boolean>()

    // stores the user's info
    private val mUser: UserType

    // sends network requests
    private val userInteractionsRepo: UserInteractionsRepo

    private val errorMessage = MutableLiveData<ErrorMessage>()

    private val cacheHandler = CacheHandler(context)

    init {
        userInteractionsRepo = UserInteractionsRepo.getInstance(context)
        mUser = userInteractionsRepo.getCachedInfo()
    }

    fun isFetching() = isFetching as LiveData<Boolean>

    fun getUser() = mUser

    fun isInteractionSuccessful() = interactionSuccessful as LiveData<Boolean>

    fun getErrorMessage() = errorMessage as LiveData<ErrorMessage>

    /**
     * sends a request to follow an account.
     */
    fun followUser(userId: String) {
        isFetching.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val followed = userInteractionsRepo.followUser(userId)

            // check if the request was successful
            if (!followed) {
                isFetching.postValue(false)
                errorMessage.postValue(userInteractionsRepo.errorMessage)
                return@launch
            }
            // update the user's "following" list in the cache
            updateCachedFollowing()
            interactionSuccessful.postValue(true)
            isFetching.postValue(false)
            return@launch

        }
    }

    /**
     * sends a request to unfollow an account.
     */
    fun unfollowUser(userId: String) {
        isFetching.value = true

        CoroutineScope(Dispatchers.IO).launch {
            val unfollowed = userInteractionsRepo.unfollowUser(userId)

            if (!unfollowed) {
                isFetching.postValue(false)
                errorMessage.postValue(userInteractionsRepo.errorMessage)
                return@launch
            }

            // update the user's "following" list in the cache
            updateCachedFollowing(false)
            isFetching.postValue(false)
            interactionSuccessful.postValue(true)
        }
    }

    /**
     * updates the user's "following" list in the cache
     */
    private fun updateCachedFollowing(add: Boolean = true) {
        val cache = cacheHandler.getCache()
        val mId = cache.getString("id")
        val following = ArrayList<String>()
        val cachedList = cache.getJSONArray("following")
        // convert the following list to an arraylist to manipulate its values
        for (i in 0 until cachedList.length()) {
            following.add(cachedList[i].toString())
        }

        // add/remove the account's id
        if (add) {
            following.add(mId)
        } else {
            following.remove(mId)
        }

        // save changes in the cache file
        cache.put("following", JSONArray(following))
        cacheHandler.storeLoginCache(cache)
    }
}
